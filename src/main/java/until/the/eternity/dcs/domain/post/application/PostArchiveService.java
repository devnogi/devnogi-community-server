package until.the.eternity.dcs.domain.post.application;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostArchive;
import until.the.eternity.dcs.domain.post.infrastructure.PostArchiveRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostLikeRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.infrastructure.PostTagRepository;

@Service
@RequiredArgsConstructor
public class PostArchiveService {
    private final PostArchiveRepository postArchiveRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMetaRepository postMetaRepository;
    private final PostMetaService postMetaService;

    @Transactional
    public void archiveOldPost(LocalDateTime date) {
        List<Post> postList = postRepository.findAllByIsDeletedTrueAndDeletedAtLessThanEqual(date);
        if (postList.isEmpty()) {
            return;
        }
        List<Long> postIdList = postList.stream().map(Post::getId).collect(Collectors.toList());
        List<PostArchive> postArchiveList =
                postList.stream().map(PostArchive::from).collect(Collectors.toList());
        postTagRepository.deleteAllByPostIdIn(postIdList); // 태그 제거
        postLikeRepository.deleteAllByPostIdIn(postIdList); // 좋아요 제거
        postMetaRepository.deleteAllByPostIdIn(postIdList); // 메타 테이블 제거
        for (Long postId : postIdList) {
            postMetaService.deletePostMeta(postId);
        }
        postRepository.deleteAllByIdIn(postIdList); // post 제거
        postArchiveRepository.saveAll(postArchiveList);
    }
}
