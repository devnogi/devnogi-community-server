package until.the.eternity.dcs.domain.postLike.application;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.post.entity.Post;
import static org.mockito.BDDMockito.*;

import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.postLike.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.postLike.dto.response.PostLikeResponse;
import until.the.eternity.dcs.domain.postLike.entity.PostLike;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("PostLikeConverter 테스트")
public class PostLikeConverterTest {

    @InjectMocks
    private PostLikeConverter postLikeConverter;

    private Post mockPost;

    @Mock
    private PostRepository mockPostRepository;

    @BeforeEach
    void setUp(){
        mockPost = Post.builder()
                .id(1L)
                .build();
    }


    @Test
    @DisplayName("requestToPostLike 테스트")
    void fromPostLikeCreateRequestToPostLike_Success(){
        //given
        PostLikeCreateRequest postLikeCreateRequest = new PostLikeCreateRequest(1L);
        given(mockPostRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(1L)).willReturn(Optional.of(mockPost));

        //when
        PostLike result = postLikeConverter.fromPostLikeCreateRequestToPostLike(postLikeCreateRequest);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getPost()).isEqualTo(mockPost);

    }

    @Test
    @DisplayName("postLikeToResponse 테스트")
    void fromPostLikeToPostLikeResponse_Success(){

        //given
        PostLike postLike = PostLike.builder()
                .id(1L)
                .post(mockPost)
                .build();

        //when
        PostLikeResponse result = postLikeConverter.fromPostLikeToPostLikeResponse(postLike);

        //then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(postLike.getId());

    }
}
