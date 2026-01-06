package until.the.eternity.dcs.domain.post.application;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.common.infrastructure.MinioService;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostArchive;
import until.the.eternity.dcs.domain.post.entity.PostImage;
import until.the.eternity.dcs.domain.post.infrastructure.PostArchiveRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostLikeRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.infrastructure.PostTagRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostArchiveService {
    private final PostArchiveRepository postArchiveRepository;
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMetaRepository postMetaRepository;
    private final PostMetaService postMetaService;
    private final MinioService minioService;

    @Transactional
    public void archiveOldPost(LocalDateTime date) {
        List<Post> postList = postRepository.findAllByIsDeletedTrueAndDeletedAtLessThanEqual(date);
        if (postList.isEmpty()) {
            return;
        }
        List<Long> postIdList = postList.stream().map(Post::getId).collect(Collectors.toList());
        List<PostArchive> postArchiveList =
                postList.stream().map(PostArchive::from).collect(Collectors.toList());
        postTagRepository.deleteAllByPostIdIn(postIdList);
        postLikeRepository.deleteAllByPostIdIn(postIdList);
        postMetaRepository.deleteAllByPostIdIn(postIdList);
        List<String> filesToDelete = new ArrayList<>();
        for (Post post : postList) {
            filesToDelete.addAll(
                    post.getImages().stream()
                            .map(PostImage::getStoredFileName)
                            .collect(Collectors.toList()));
        }
        for (Long postId : postIdList) {
            postMetaService.deletePostMeta(postId);
        }
        postRepository.deleteAllByIdIn(postIdList);
        postArchiveRepository.saveAll(postArchiveList);
        for (String fileName : filesToDelete) {
            try {
                minioService.deleteFile(fileName);
            } catch (Exception e) {
                log.error("MinIO 파일 삭제 실패 (파일명: {}): {}", fileName, e.getMessage());
            }
        }
    }
}
