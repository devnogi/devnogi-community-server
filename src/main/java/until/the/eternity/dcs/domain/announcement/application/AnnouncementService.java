package until.the.eternity.dcs.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPageResponseItem;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.announcement.entity.AnnouncementRepository;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementDuplicateException;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementNotFoundException;
import until.the.eternity.dcs.domain.board.entity.BoardRepository;
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
    private final BoardRepository boardRepository;

    @Transactional
    public AnnouncementPersistResponse create(Long postId, AnnouncementCreateRequest request) {
        // todo board에 announcement리스트도 추가
        duplicateCheck(postId);

        Post post = getPost(postId);
        PostMeta postMeta = getPostMeta(postId);

        Announcement announcement = converter.fromCreateRequestAndPost(request, post, postMeta);
        Announcement saved = repository.save(announcement);
        postMetaRepository.save(postMeta);

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

    public Page<AnnouncementPageResponseItem> getAnnouncementByBoardId(Long boardId) {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Announcement> announcements = repository.findByBoardIdAndGlobal(boardId, pageable);

        return announcements.map(converter::fromEntityToPageResponse);
    }
}
