package until.the.eternity.dcs.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.announcement.entity.AnnouncementRepository;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementDuplicateException;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementNotFoundException;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository repository;
    private final AnnouncementConverter converter;
    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;

    @Transactional
    public AnnouncementPersistResponse create(Long postId, AnnouncementCreateRequest request) {
        if (repository.existsByPostId(postId)) {
            throw new AnnouncementDuplicateException();
        }

        Post post =
                postRepository
                        .findByIdAndIsDeletedFalseAndIsBlockedFalse(postId)
                        .orElseThrow(() -> new PostNotFoundException(postId));
        PostMeta postMeta = postMetaRepository.findByPostId(postId).orElse(PostMeta.create(postId));

        Announcement announcement = converter.fromCreateRequestAndPost(request, post, postMeta);

        Announcement saved = repository.save(announcement);
        postMetaRepository.save(postMeta);

        return converter.fromEntityToPersistResponse(saved);
    }

    public void delete(Long id) {
        // todo 유저 확인
        repository.deleteById(id);
    }

    @Transactional
    public AnnouncementToggleResponse toggleGlobal(Long id) {
        Announcement announcement =
                repository.findById(id).orElseThrow(() -> new AnnouncementNotFoundException(id));

        announcement.toggleIsGlobal();

        return converter.fromEntityToToggleResponse(announcement);
    }
}
