package until.the.eternity.dcs.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPageResponseItem;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementBoardNotFoundException;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementDuplicateException;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementNotFoundException;
import until.the.eternity.dcs.domain.announcement.infrastructure.AnnouncementRepository;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.infrastructure.BoardRepository;
import until.the.eternity.dcs.domain.post.application.PostMetaService;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;

import java.util.List;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.ANNOUNCEMENT;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository repository;
    private final AnnouncementConverter converter;
    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;
    private final PostMetaService postMetaService;
    private final RedisSender redisSender;
    private final AnnouncementPermissionEvaluator announcementPermissionEvaluator;
    private final BoardRepository boardRepository;

    @Transactional
    public AnnouncementPersistResponse create(Long postId, AnnouncementCreateRequest request) {
        duplicateCheck(postId);

        Post post = getPost(postId);
        PostMetaResponse postMeta = getPostMetaInfo(postId);

        Announcement announcement = converter.fromCreateRequestAndPost(request, post, postMeta);
        Announcement saved = repository.save(announcement);

        Board board = post.getBoard();
        board.getAnnouncements().add(announcement);

        // 0L로 설정 -> 전체 유저에게 전송
        redisSender.enqueue(NotificationJob.of(0L, ANNOUNCEMENT, postId));

        return converter.fromEntityToPersistResponse(saved);
    }

    @Transactional
    @PreAuthorize("@announcementPermissionEvaluator.canDelete(authentication,#id)")
    public void delete(Long id) {

        repository.deleteById(id);
    }

    @Transactional
    public AnnouncementToggleResponse toggleGlobal(Long id) {
        Announcement announcement = findById(id);

        announcement.toggleIsGlobal();

        return converter.fromEntityToToggleResponse(announcement);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementPageResponseItem> getAnnouncements(Long boardId) {
        List<Announcement> announcements;

        if (boardId == null) {
            announcements = repository.findByIsDraftFalseAndIsGlobalTrue();
        } else {
            validateBoard(boardId);
            announcements = repository.findActiveByBoardIdOrGlobal(boardId);
        }

        return announcements.stream().map(converter::fromEntityToPageResponse).toList();
    }

    private void duplicateCheck(Long postId) {
        if (repository.existsByPostId(postId)) {
            throw new AnnouncementDuplicateException();
        }
    }

    private Post getPost(Long postId) {
        return postRepository
                .findByIdAndIsDeletedFalseAndIsBlockedFalse(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private PostMetaResponse getPostMetaInfo(Long postId) {
        return postMetaService.getPostMetaInfo(postId);
    }

    private Announcement findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new AnnouncementNotFoundException(id));
    }

    private void validateBoard(Long boardId) {
        boardRepository
                .findByIdAndIsDeletedIsFalse(boardId)
                .orElseThrow(() -> new AnnouncementBoardNotFoundException(boardId));
    }
}
