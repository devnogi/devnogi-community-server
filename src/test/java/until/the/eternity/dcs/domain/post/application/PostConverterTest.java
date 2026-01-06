package until.the.eternity.dcs.domain.post.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostConverter 테스트")
public class PostConverterTest {

    @Mock private PostMeta postMeta;
    @InjectMocks private PostConverter postConverter;

    @Test
    @DisplayName("정상적인 PostCreateRequest로 Post 객체를 생성한다")
    void should_CreatePost_When_ValidRequest() {

        // given
        Long boardId = 1L;
        String title = "테스트 게시글 제목";
        String content = "테스트 게시글 내용";
        Boolean isDraft = false;

        Long userId = 100L;
        List<String> stringTagList = Arrays.asList("test1", "test2");

        List<PostTag> postTagList =
                Arrays.asList(PostTag.builder().id(1L).build(), PostTag.builder().id(2L).build());

        PostCreateRequest request =
                new PostCreateRequest(boardId, title, content, isDraft, stringTagList);

        // when
        Post result = postConverter.fromCreateRequestToPost(request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBoard().getId()).isEqualTo(boardId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getIsDraft()).isEqualTo(isDraft);
    }

    @Test
    @DisplayName("정상적인 Post로 PostSummaryResponse를 생성한다")
    void should_CreatePostSummaryResponse_When_ValidPost() {

        // given
        Long boardId = 1L;
        String title = "테스트 게시글 제목";
        String content = "테스트 게시글 내용";
        Boolean isDraft = false;

        Post post = Post.builder().id(1L).title(title).build();
        post.setCreatedAt(LocalDateTime.now());
        PostMetaResponse postMetaResponse = PostMetaResponse.from(postMeta);
        // when
        PostSummaryResponse result =
                postConverter.fromPostToPostSummaryResponse(post, postMetaResponse);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(post.getId());
        assertThat(result.title()).isEqualTo(post.getTitle());
        assertThat(result.viewCount()).isEqualTo(postMeta.getViewCount());
        assertThat(result.likeCount()).isEqualTo(postMeta.getLikeCount());
        assertThat(result.commentCount()).isEqualTo(postMeta.getCommentCount());
        assertThat(result.createdAt()).isEqualTo(post.getCreatedAt());
    }

    @Test
    @DisplayName("정상적인 Post와 댓글 리스트로 PostDetailResponse를 생성한다")
    void should_CreatePostDetailResponse_When_ValidPostAndComments() {

        // given
        Long boardId = 1L;
        String title = "테스트 게시글 제목";
        String content = "테스트 게시글 내용";
        Boolean isDraft = false;
        Board board = Board.builder().id(boardId).build();
        List<PostTag> postTags = new ArrayList<>(); // 우선 빈 리스트 사용
        List<String> imageUrlList = new ArrayList<>();

        Post post =
                Post.builder()
                        .id(1L)
                        .board(board)
                        .title(title)
                        .content(content)
                        .isDraft(isDraft)
                        .isBlocked(false)
                        .postTags(postTags)
                        .build();
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        PostMetaResponse postMetaResponse = PostMetaResponse.from(postMeta);

        // when
        PostDetailResponse result =
                postConverter.fromPostToPostDetailResponse(post, postMetaResponse, imageUrlList);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(post.getId());
        assertThat(result.boardId()).isEqualTo(post.getBoard().getId());
        assertThat(result.userId()).isEqualTo(post.getUserId());
        assertThat(result.title()).isEqualTo(post.getTitle());
        assertThat(result.content()).isEqualTo(post.getContent());
        assertThat(result.viewCount()).isEqualTo(postMeta.getViewCount());
        assertThat(result.likeCount()).isEqualTo(postMeta.getLikeCount());
        assertThat(result.commentCount()).isEqualTo(postMeta.getCommentCount());
        assertThat(result.isDraft()).isEqualTo(post.getIsDraft());
        assertThat(result.isBlocked()).isEqualTo(post.getIsBlocked());
        assertThat(result.createdAt()).isEqualTo(post.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(post.getUpdatedAt());
    }
}
