package until.the.eternity.dcs.domain.post.application;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import until.the.eternity.dcs.common.infrastructure.MinioService;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostImage;
import until.the.eternity.dcs.domain.post.infrastructure.*;
import until.the.eternity.dcs.domain.tag.infrastructure.PostTagRepository;

class PostArchiveServiceTest {
    private final PostArchiveRepository archiveRepository = mock(PostArchiveRepository.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final PostTagRepository tagRepository = mock(PostTagRepository.class);
    private final PostLikeRepository likeRepository = mock(PostLikeRepository.class);
    private final PostBookmarkRepository bookmarkRepository = mock(PostBookmarkRepository.class);
    private final PostMetaRepository metaRepository = mock(PostMetaRepository.class);
    private final PostMetaService metaService = mock(PostMetaService.class);
    private final MinioService minioService = mock(MinioService.class);
    private final PostArchiveService service =
            new PostArchiveService(
                    archiveRepository,
                    postRepository,
                    tagRepository,
                    likeRepository,
                    bookmarkRepository,
                    metaRepository,
                    metaService,
                    minioService);

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void deletesMinioObjectsOnlyAfterCommit() {
        Post post =
                Post.builder()
                        .id(1L)
                        .title("title")
                        .content("content")
                        .userId(1L)
                        .images(
                                new java.util.ArrayList<>(
                                        List.of(
                                                PostImage.builder()
                                                        .storedFileName("stored.png")
                                                        .build())))
                        .build();
        when(postRepository.findAllByIsDeletedTrueAndDeletedAtLessThanEqual(any()))
                .thenReturn(List.of(post));
        TransactionSynchronizationManager.initSynchronization();

        service.archiveOldPost(LocalDateTime.now());

        verify(minioService, never()).deleteFile(anyString());
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(minioService).deleteFile("stored.png");
    }
}
