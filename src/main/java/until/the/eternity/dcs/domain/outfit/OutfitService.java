package until.the.eternity.dcs.domain.outfit;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import until.the.eternity.dcs.common.infrastructure.MinioService;
import until.the.eternity.dcs.domain.outfit.catalog.OutfitCatalogService;
import until.the.eternity.dcs.domain.post.application.PostMediaValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutfitService {
    private final OutfitRepository repository;
    private final MinioService storage;
    private final OutfitCatalogService catalog;
    private final TransactionTemplate transaction;

    static long currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof Long id)
                || id <= 0)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return id;
    }

    private void owner(OutfitRepository.PostRow post, long user) {
        if (post.userId() != user)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 변경할 수 있습니다.");
    }

    private static LocalDateTime now() {
        return LocalDateTime.now(Clock.systemUTC());
    }

    private OutfitDtos.Author author(long id, Map<Long, OutfitDtos.Author> authors) {
        return authors.getOrDefault(id, new OutfitDtos.Author(id, "사용자", null, null, false));
    }

    private List<OutfitDtos.Outfit> hydrate(List<OutfitRepository.PostRow> rows) {
        if (rows.isEmpty()) return List.of();
        List<Long> ids = rows.stream().map(OutfitRepository.PostRow::id).toList();
        var images = repository.images(ids);
        var items = repository.items(ids);
        var tags = repository.tags(ids);
        var authors =
                repository.authors(
                        rows.stream().map(OutfitRepository.PostRow::userId).distinct().toList());
        return rows.stream()
                .map(
                        p ->
                                new OutfitDtos.Outfit(
                                        p.id(),
                                        p.userId(),
                                        author(p.userId(), authors),
                                        p.content(),
                                        images.getOrDefault(p.id(), List.of()).stream()
                                                .map(
                                                        i ->
                                                                new OutfitDtos.Image(
                                                                        i.id(),
                                                                        storage.getFileUrl(
                                                                                i.storedName()),
                                                                        i.displayOrder()))
                                                .toList(),
                                        items.getOrDefault(p.id(), List.of()),
                                        tags.getOrDefault(p.id(), List.of()),
                                        p.likes(),
                                        p.comments(),
                                        p.shares(),
                                        p.createdAt().toInstant(ZoneOffset.UTC),
                                        p.updatedAt().toInstant(ZoneOffset.UTC)))
                .toList();
    }

    public OutfitDtos.Outfit detail(long id) {
        return hydrate(List.of(repository.post(id, false))).getFirst();
    }

    public OutfitDtos.Page<OutfitDtos.Outfit> list(
            String sort,
            int size,
            int page,
            String cursor,
            List<OutfitDtos.Item> filters,
            List<String> tags,
            String motion,
            String scope) {
        if (!Set.of("LATEST", "POPULAR").contains(sort)
                || size < 1
                || size > 50
                || page < 0
                || page > 10000
                || filters.size() > 11) throw OutfitValidation.invalid("잘못된 검색 조건입니다.");
        List<OutfitDtos.Item> valid =
                filters.stream().map(i -> OutfitValidation.item(i, true)).toList();
        if (valid.stream()
                        .filter(i -> i.slot() != null && i.slot().startsWith("ACCESSORY_"))
                        .count()
                > 2) throw OutfitValidation.invalid("액세서리는 최대 2개입니다.");
        Long user = scope == null ? null : currentUser();
        List<OutfitRepository.PostRow> all =
                repository.list(
                        sort,
                        size,
                        page,
                        cursor,
                        valid,
                        OutfitValidation.tags(tags),
                        motion,
                        user,
                        "saved".equals(scope));
        boolean more = all.size() > size;
        List<OutfitRepository.PostRow> rows = all.stream().limit(size).toList();
        String next = null;
        if (more && !rows.isEmpty() && sort.equals("LATEST")) {
            var last = rows.getLast();
            next =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(
                                    (last.createdAt() + "|" + last.id())
                                            .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return new OutfitDtos.Page<>(hydrate(rows), next, more);
    }

    public OutfitDtos.Outfit write(Long id, OutfitDtos.Write raw, List<MultipartFile> newFiles) {
        long user = currentUser();
        if (id != null) owner(repository.post(id, false), user);
        OutfitDtos.Write request = OutfitValidation.write(raw);
        List<MultipartFile> files = newFiles == null ? List.of() : newFiles;
        if (files.size() > 5) throw OutfitValidation.invalid("사진은 최대 5장입니다.");
        List<OutfitRepository.ImageRow> before = id == null ? List.of() : repository.images(id);
        validateMedia(request.mediaOrder(), files.size(), before);
        for (MultipartFile file : files) {
            var media = PostMediaValidator.validate(file);
            if (!media.mimeType().startsWith("image/"))
                throw OutfitValidation.invalid("사진만 업로드할 수 있습니다.");
        }
        List<OutfitDtos.Item> oldItems =
                id == null ? List.of() : repository.items(List.of(id)).getOrDefault(id, List.of());
        catalog.validateItems(
                request.items().stream()
                        .filter(i -> i.inputType().equals("CATALOG") && !oldItems.contains(i))
                        .map(
                                i ->
                                        new OutfitCatalogService.Selection(
                                                i.slot(),
                                                new OutfitCatalogService.Item(
                                                        i.name(),
                                                        i.topCategory(),
                                                        i.subCategory())))
                        .toList());
        List<String> uploaded = new ArrayList<>();
        Long saved;
        try {
            for (MultipartFile file : files) uploaded.add(storage.uploadFile(file));
            saved =
                    transaction.execute(
                            status -> {
                                long postId;
                                LocalDateTime time = now();
                                List<OutfitRepository.ImageRow> existing;
                                if (id == null) {
                                    postId =
                                            repository.insert(
                                                    "INSERT INTO outfit_post(user_id,content,created_at,updated_at) VALUES(:user,:content,:now,:now)",
                                                    new MapSqlParameterSource("user", user)
                                                            .addValue("content", request.content())
                                                            .addValue("now", time));
                                    existing = List.of();
                                } else {
                                    owner(repository.post(id, true), user);
                                    postId = id;
                                    existing = repository.images(id);
                                    repository.update(
                                            "UPDATE outfit_post SET content=:content,updated_at=:now WHERE id=:id",
                                            Map.of(
                                                    "content",
                                                    request.content(),
                                                    "now",
                                                    time,
                                                    "id",
                                                    id));
                                }
                                validateMedia(request.mediaOrder(), files.size(), existing);
                                Set<Long> retained = new HashSet<>();
                                for (int index = 0; index < request.mediaOrder().size(); index++) {
                                    var order = request.mediaOrder().get(index);
                                    if (order.imageId() != null) {
                                        retained.add(order.imageId());
                                        repository.update(
                                                "UPDATE outfit_image SET display_order=:position WHERE id=:image AND outfit_id=:id",
                                                Map.of(
                                                        "position",
                                                        index,
                                                        "image",
                                                        order.imageId(),
                                                        "id",
                                                        postId));
                                    } else {
                                        int upload = order.uploadIndex();
                                        var media = PostMediaValidator.validate(files.get(upload));
                                        repository.insert(
                                                "INSERT INTO outfit_image(outfit_id,stored_name,mime_type,byte_size,display_order) VALUES(:post,:name,:mime,:bytes,:position)",
                                                new MapSqlParameterSource("post", postId)
                                                        .addValue("name", uploaded.get(upload))
                                                        .addValue("mime", media.mimeType())
                                                        .addValue(
                                                                "bytes",
                                                                files.get(upload).getSize())
                                                        .addValue("position", index));
                                    }
                                }
                                List<String> removed = new ArrayList<>();
                                for (var image : existing) {
                                    if (!retained.contains(image.id())) {
                                        repository.update(
                                                "DELETE FROM outfit_image WHERE id=:id",
                                                Map.of("id", image.id()));
                                        removed.add(image.storedName());
                                    }
                                }
                                replaceItems(postId, request);
                                afterCommitDelete(removed);
                                return postId;
                            });
        } catch (RuntimeException ex) {
            uploaded.forEach(this::safeDelete);
            throw ex;
        }
        return detail(Objects.requireNonNull(saved));
    }

    static void validateMedia(
            List<OutfitDtos.MediaOrder> order,
            int uploads,
            List<OutfitRepository.ImageRow> existing) {
        Set<Long> allowed = new HashSet<>();
        existing.forEach(i -> allowed.add(i.id()));
        Set<Long> retained = new HashSet<>();
        Set<Integer> added = new HashSet<>();
        for (var entry : order) {
            if (entry == null || (entry.imageId() == null) == (entry.uploadIndex() == null))
                throw OutfitValidation.invalid("잘못된 사진 순서입니다.");
            if (entry.imageId() != null) {
                if (!allowed.contains(entry.imageId()) || !retained.add(entry.imageId()))
                    throw OutfitValidation.invalid("사진 소유권 또는 중복을 확인해 주세요.");
            } else if (entry.uploadIndex() < 0
                    || entry.uploadIndex() >= uploads
                    || !added.add(entry.uploadIndex()))
                throw OutfitValidation.invalid("잘못된 새 사진 참조입니다.");
        }
        if (added.size() != uploads) throw OutfitValidation.invalid("모든 새 사진의 순서를 지정해 주세요.");
    }

    private void replaceItems(long id, OutfitDtos.Write request) {
        repository.update("DELETE FROM outfit_item WHERE outfit_id=:id", Map.of("id", id));
        for (var i : request.items())
            repository.insert(
                    "INSERT INTO outfit_item(outfit_id,slot,input_type,name,top_category,sub_category,normalized_name) VALUES(:id,:slot,:type,:name,:top,:sub,:normalized)",
                    new MapSqlParameterSource("id", id)
                            .addValue("slot", i.slot())
                            .addValue("type", i.inputType())
                            .addValue("name", i.name())
                            .addValue("top", i.topCategory())
                            .addValue("sub", i.subCategory())
                            .addValue("normalized", OutfitValidation.normalized(i.name())));
        repository.update("DELETE FROM outfit_tag WHERE outfit_id=:id", Map.of("id", id));
        for (String tag : request.tags())
            repository.insert(
                    "INSERT INTO outfit_tag(outfit_id,tag,normalized_tag) VALUES(:id,:tag,:normalized)",
                    new MapSqlParameterSource("id", id)
                            .addValue("tag", tag)
                            .addValue("normalized", OutfitValidation.normalized(tag)));
    }

    private void afterCommitDelete(List<String> files) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        files.forEach(OutfitService.this::safeDelete);
                    }
                });
    }

    private void safeDelete(String file) {
        try {
            storage.deleteFile(file);
        } catch (RuntimeException ex) {
            log.error("의장 이미지 정리 실패: {}", file, ex);
        }
    }

    public void delete(long id) {
        long user = currentUser();
        transaction.executeWithoutResult(
                status -> {
                    owner(repository.post(id, true), user);
                    List<String> names =
                            repository.images(id).stream()
                                    .map(OutfitRepository.ImageRow::storedName)
                                    .toList();
                    repository.update(
                            "UPDATE outfit_post SET is_deleted=true,updated_at=:now WHERE id=:id",
                            Map.of("now", now(), "id", id));
                    afterCommitDelete(names);
                });
    }

    public List<OutfitDtos.ViewerState> states(List<Long> ids) {
        long user = currentUser();
        if (ids.size() > 50 || ids.stream().anyMatch(id -> id == null || id <= 0))
            throw OutfitValidation.invalid("잘못된 의장 목록입니다.");
        if (ids.isEmpty()) return List.of();
        var likes = repository.reactionIds("outfit_like", user, ids);
        var saved = repository.reactionIds("outfit_bookmark", user, ids);
        return ids.stream()
                .distinct()
                .map(id -> new OutfitDtos.ViewerState(id, likes.contains(id), saved.contains(id)))
                .toList();
    }

    public OutfitDtos.ViewerState reaction(long id, boolean like, boolean enabled) {
        long user = currentUser();
        transaction.executeWithoutResult(
                status -> {
                    repository.post(id, true);
                    String table = like ? "outfit_like" : "outfit_bookmark";
                    Map<String, Object> params = Map.of("id", id, "user", user, "now", now());
                    long count =
                            repository.count(
                                    "SELECT COUNT(*) FROM "
                                            + table
                                            + " WHERE outfit_id=:id AND user_id=:user",
                                    params);
                    if (enabled && count == 0) {
                        repository.update(
                                "INSERT INTO "
                                        + table
                                        + "(outfit_id,user_id,created_at) VALUES(:id,:user,:now)",
                                params);
                        if (like)
                            repository.update(
                                    "UPDATE outfit_post SET like_count=like_count+1 WHERE id=:id",
                                    params);
                    } else if (!enabled && count > 0) {
                        repository.update(
                                "DELETE FROM " + table + " WHERE outfit_id=:id AND user_id=:user",
                                params);
                        if (like)
                            repository.update(
                                    "UPDATE outfit_post SET like_count=like_count-1 WHERE id=:id",
                                    params);
                    }
                });
        return states(List.of(id)).getFirst();
    }

    public OutfitDtos.ShareResult share(long id, OutfitDtos.ShareWrite input) {
        long user = currentUser();
        if (input == null
                || input.requestId() == null
                || input.channel() == null
                || !Set.of("NATIVE", "CLIPBOARD").contains(input.channel()))
            throw OutfitValidation.invalid("잘못된 공유 정보입니다.");
        try {
            if (!UUID.fromString(input.requestId()).toString().equals(input.requestId()))
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw OutfitValidation.invalid("잘못된 공유 요청 ID입니다.");
        }
        return transaction.execute(
                status -> {
                    repository.post(id, true);
                    Map<String, Object> p =
                            Map.of(
                                    "id",
                                    id,
                                    "user",
                                    user,
                                    "request",
                                    input.requestId(),
                                    "channel",
                                    input.channel(),
                                    "now",
                                    now());
                    if (repository.count(
                                    "SELECT COUNT(*) FROM outfit_share WHERE outfit_id=:id AND user_id=:user AND request_id=:request",
                                    p)
                            == 0) {
                        repository.update(
                                "INSERT INTO outfit_share(outfit_id,user_id,request_id,channel,created_at) VALUES(:id,:user,:request,:channel,:now)",
                                p);
                        repository.update(
                                "UPDATE outfit_post SET share_count=share_count+1 WHERE id=:id", p);
                    }
                    return new OutfitDtos.ShareResult(repository.post(id, false).shares());
                });
    }

    public OutfitDtos.Page<OutfitDtos.Comment> comments(long id, Long parent, int page, int size) {
        repository.post(id, false);
        if (page < 0 || page > 10000 || size < 1 || size > 50)
            throw OutfitValidation.invalid("잘못된 페이지입니다.");
        if (parent != null && repository.comment(parent, id).parentId() != null)
            throw OutfitValidation.invalid("답글은 한 단계만 지원합니다.");
        var rows = repository.comments(id, parent, page, size);
        boolean more = rows.size() > size;
        return new OutfitDtos.Page<>(comments(rows.stream().limit(size).toList()), null, more);
    }

    private List<OutfitDtos.Comment> comments(List<OutfitRepository.CommentRow> rows) {
        if (rows.isEmpty()) return List.of();
        var authors =
                repository.authors(
                        rows.stream().map(OutfitRepository.CommentRow::userId).distinct().toList());
        return rows.stream()
                .map(
                        c ->
                                new OutfitDtos.Comment(
                                        c.id(),
                                        c.outfitId(),
                                        c.userId(),
                                        author(c.userId(), authors),
                                        c.deleted() ? "" : c.content(),
                                        c.parentId(),
                                        c.deleted(),
                                        c.replies(),
                                        c.createdAt().toInstant(ZoneOffset.UTC),
                                        c.updatedAt().toInstant(ZoneOffset.UTC)))
                .toList();
    }

    public OutfitDtos.Comment comment(long id, Long commentId, OutfitDtos.CommentWrite input) {
        long user = currentUser();
        String content = OutfitValidation.text(input == null ? null : input.content(), 1000, true);
        Long saved =
                transaction.execute(
                        status -> {
                            repository.post(id, true);
                            if (commentId != null) {
                                var old = repository.comment(commentId, id);
                                if (old.userId() != user)
                                    throw new ResponseStatusException(
                                            HttpStatus.FORBIDDEN, "작성자만 변경할 수 있습니다.");
                                if (old.deleted())
                                    throw new ResponseStatusException(
                                            HttpStatus.NOT_FOUND, "삭제된 댓글입니다.");
                                repository.update(
                                        "UPDATE outfit_comment SET content=:content,updated_at=:now WHERE id=:id",
                                        Map.of("content", content, "now", now(), "id", commentId));
                                return commentId;
                            }
                            Long parent = input.parentId();
                            if (parent != null) {
                                var original = repository.comment(parent, id);
                                if (original.deleted() || original.parentId() != null)
                                    throw OutfitValidation.invalid("답글을 작성할 수 없는 댓글입니다.");
                            }
                            long newId =
                                    repository.insert(
                                            "INSERT INTO outfit_comment(outfit_id,user_id,parent_id,content,created_at,updated_at) VALUES(:post,:user,:parent,:content,:now,:now)",
                                            new MapSqlParameterSource("post", id)
                                                    .addValue("user", user)
                                                    .addValue("parent", parent)
                                                    .addValue("content", content)
                                                    .addValue("now", now()));
                            repository.update(
                                    "UPDATE outfit_post SET comment_count=comment_count+1 WHERE id=:id",
                                    Map.of("id", id));
                            return newId;
                        });
        return comments(List.of(repository.comment(Objects.requireNonNull(saved), id))).getFirst();
    }

    public void deleteComment(long id, long comment) {
        long user = currentUser();
        transaction.executeWithoutResult(
                status -> {
                    repository.post(id, true);
                    var old = repository.comment(comment, id);
                    if (old.userId() != user)
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다.");
                    if (!old.deleted()) {
                        repository.update(
                                "UPDATE outfit_comment SET is_deleted=true,content='',updated_at=:now WHERE id=:id",
                                Map.of("now", now(), "id", comment));
                        repository.update(
                                "UPDATE outfit_post SET comment_count=comment_count-1 WHERE id=:id",
                                Map.of("id", id));
                    }
                });
    }

    public String image(long id, long image) {
        repository.post(id, false);
        return repository.images(id).stream()
                .filter(i -> i.id() == image)
                .findFirst()
                .map(i -> storage.getFileUrl(i.storedName()))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사진을 찾을 수 없습니다."));
    }

    public List<String> motions(String query) {
        return repository.motions(OutfitValidation.text(query, 100, false));
    }
}
