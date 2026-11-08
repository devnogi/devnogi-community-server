package until.the.eternity.dcs.domain.post.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.post.exception.PostMetaInvariantViolationException;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;

class PostMetaServiceTest {
    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    private final PostMetaRepository postMetaRepository = mock(PostMetaRepository.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final PostMetaService service =
            new PostMetaService(redisTemplate, postMetaRepository, commentRepository);

    @Test
    void addCommentUpdatesDatabaseWithoutWritingRedisDelta() {
        when(postMetaRepository.incrementCommentCount(7L, 1)).thenReturn(1);

        service.addComment(7L);

        verify(postMetaRepository).incrementCommentCount(7L, 1);
        verify(redisTemplate, never()).opsForHash();
    }

    @Test
    void deleteCommentUpdatesDatabaseWithoutWritingRedisDelta() {
        when(postMetaRepository.incrementCommentCount(7L, -1)).thenReturn(1);

        service.deleteComment(7L);

        verify(postMetaRepository).incrementCommentCount(7L, -1);
        verify(redisTemplate, never()).opsForHash();
    }

    @Test
    void missingPostMetaFailsWithoutReapplyingResidualRedisDelta() {
        when(postMetaRepository.incrementCommentCount(7L, 1)).thenReturn(0);

        assertThatThrownBy(() -> service.addComment(7L))
                .isInstanceOf(PostMetaInvariantViolationException.class)
                .hasMessage("Post metadata is missing for postId=7");

        verify(commentRepository, never()).countByPostIdAndIsDeletedFalse(7L);
        verify(postMetaRepository, never()).save(any());
    }
}
