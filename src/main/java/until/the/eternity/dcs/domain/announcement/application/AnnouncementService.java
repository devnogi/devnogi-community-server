package until.the.eternity.dcs.domain.announcement.application;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.ANNOUNCEMENT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPageResponseItem;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.announcement.entity.AnnouncementRepository;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementDuplicateException;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementNotFoundException;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.exception.PostModifyForbiddenException;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository repository;
    private final AnnouncementConverter converter;
    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;
    private final UserService userService;
    private final RedisSender redisSender;

    @Transactional
    public AnnouncementPersistResponse create(Long postId, AnnouncementCreateRequest request) {
        duplicateCheck(postId);

        Post post = getPost(postId);
        PostMeta postMeta = getPostMeta(postId);

        Announcement announcement = converter.fromCreateRequestAndPost(request, post, postMeta);
        Announcement saved = repository.save(announcement);
        postMetaRepository.save(postMeta);

        Board board = post.getBoard();
        board.getAnnouncements().add(announcement);

        // 0L로 설정 -> 전체 유저에게 전송
        redisSender.enqueue(NotificationJob.of(0L, ANNOUNCEMENT, postId));

        return converter.fromEntityToPersistResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        isAuthorizedUser(findById(id));

        repository.deleteById(id);
    }

    @Transactional
    public AnnouncementToggleResponse toggleGlobal(Long id) {
        Announcement announcement = findById(id);

        announcement.toggleIsGlobal();

        return converter.fromEntityToToggleResponse(announcement);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementPageResponseItem> getAnnouncementByBoardId(Long boardId) {
        List<Announcement> announcements = repository.findByBoardIdAndGlobal(boardId);

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

    private PostMeta getPostMeta(Long postId) {
        return postMetaRepository.findByPostId(postId).orElse(PostMeta.create(postId));
    }

    private Announcement findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new AnnouncementNotFoundException(id));
    }

    private void isAuthorizedUser(Announcement announcement) {
        UserSummary currentUser = userService.getCurrentUser();
        if (currentUser.getId().equals(announcement.getUserId())
                || currentUser.getGrade().equals(UserGrade.ADMIN)) {
            return;
        }
        throw new PostModifyForbiddenException();
    }
}
