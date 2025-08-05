package until.the.eternity.dcs.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.announcement.entity.AnnouncementRepository;
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

    public AnnouncementPersistResponse create(Long postId, AnnouncementCreateRequest request) {
        Post post =
                postRepository
                        .findByIdAndIsDeletedFalseAndIsBlockedFalse(postId)
                        .orElseThrow(() -> new PostNotFoundException(postId));
        PostMeta postMeta = postMetaRepository.findByPostId(postId).orElse(PostMeta.create(postId));

        Announcement announcement = converter.fromCreateRequestAndPost(request, post, postMeta);

        Announcement saved = repository.save(announcement);

        return converter.fromEntityToPersistResponse(saved);
    }
}
