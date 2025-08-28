package until.the.eternity.dcs.domain.post.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.post.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostLike;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostLikeConverter 테스트")
public class PostLikeConverterTest {

    private PostLikeConverter postLikeConverter;
    Long userId = 1L;
    private Post mockPost;

    @BeforeEach
    void setUp() {
        postLikeConverter = new PostLikeConverter();

        mockPost = Post.builder().id(1L).build();
    }

    @Test
    @DisplayName("requestToPostLike 테스트")
    void toEntity_Success() {
        // given
        PostLikeCreateRequest postLikeCreateRequest = new PostLikeCreateRequest(1L, userId);

        // when
        PostLike result = postLikeConverter.toEntity(1L, mockPost);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPost()).isEqualTo(mockPost);
        assertThat(result.getUserId()).isEqualTo(userId);
    }
}
