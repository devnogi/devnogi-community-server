package until.the.eternity.dcs.domain.guild;

import static until.the.eternity.dcs.domain.guild.GuildContent.error;

import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import until.the.eternity.dcs.common.infrastructure.MinioService;
import until.the.eternity.dcs.domain.post.application.PostMediaValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuildMedia {
    final GuildContent content;
    final MinioService storage;

    private String keyColumn(String kind) {
        return switch (kind) {
            case "banner" -> "banner_key";
            case "emblem" -> "emblem_key";
            default -> throw error(HttpStatus.NOT_FOUND, "이미지 종류를 확인해 주세요.");
        };
    }

    private String key(int g, Long post, String kind) {
        if (post != null) {
            content.writing(g, post, true);
            return (String)
                    content.one(
                                    "SELECT photo_key AS k FROM guild_post WHERE id=? AND guild_id=?",
                                    post,
                                    g)
                            .get("k");
        }
        return (String)
                content.one(
                                "SELECT p."
                                        + keyColumn(kind)
                                        + " AS k FROM guild_profile p JOIN guild g ON g.id=p.guild_id WHERE g.id=? AND g.is_deleted=0",
                                g)
                        .get("k");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> image(int g, Long post, String kind) throws IOException {
        String object = key(g, post, kind);
        if (object == null) throw error(HttpStatus.NOT_FOUND, "이미지가 없습니다.");
        String mime =
                object.endsWith(".jpg") || object.endsWith(".jpeg")
                        ? "image/jpeg"
                        : "image/" + object.substring(object.lastIndexOf('.') + 1);
        try (InputStream in = storage.downloadFile(object)) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .contentType(MediaType.parseMediaType(mime))
                    .header("X-Content-Type-Options", "nosniff")
                    .body(in.readAllBytes());
        }
    }

    @Transactional
    public void upload(int g, Long post, String kind, MultipartFile file) {
        content.access.lockGuild(g);
        if (post != null) content.author(g, post, true);
        else content.editor(g);
        String previous = key(g, post, kind);
        if (!PostMediaValidator.validate(file).mimeType().startsWith("image/"))
            throw error(HttpStatus.BAD_REQUEST, "이미지 파일을 선택해 주세요.");
        String next = storage.uploadFile(file);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        String stale =
                                status == STATUS_COMMITTED
                                        ? previous
                                        : status == STATUS_ROLLED_BACK ? next : null;
                        if (stale != null)
                            try {
                                storage.deleteFile(stale);
                            } catch (RuntimeException failure) {
                                log.warn("길드 이미지 정리 실패", failure);
                            }
                    }
                });
        if (post == null)
            content.db.update(
                    "UPDATE guild_profile SET "
                            + keyColumn(kind)
                            + "=?,updated_at=CURRENT_TIMESTAMP,updated_by=? WHERE guild_id=?",
                    next,
                    content.access.user(),
                    g);
        else
            content.db.update(
                    "UPDATE guild_post SET photo_key=?,updated_at=CURRENT_TIMESTAMP WHERE id=? AND guild_id=?",
                    next,
                    post,
                    g);
    }
}
