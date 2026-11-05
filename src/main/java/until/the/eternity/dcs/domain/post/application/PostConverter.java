package until.the.eternity.dcs.domain.post.application;

import java.util.List;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostPersistResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.enums.PostContentFormat;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Component
public class PostConverter {

    public Post fromCreateRequestToPost(PostCreateRequest request, Long userId) {
        boolean plainText = "PLAIN_TEXT_V1".equals(request.contentFormat());
        Board board =
                request.boardId() == null ? null : Board.builder().id(request.boardId()).build();
        Post post =
                Post.builder()
                        .board(plainText ? null : board)
                        .userId(userId)
                        .title(request.title() == null ? "게시글" : request.title())
                        .content(request.content())
                        .plainContent(plainText ? request.content() : null)
                        .contentFormat(
                                plainText
                                        ? PostContentFormat.PLAIN_TEXT_V1
                                        : PostContentFormat.LEGACY_MARKDOWN)
                        .isDraft(plainText ? false : request.isDraft())
                        .build();
        if (plainText) {
            post.updatePlainContent(request.content(), userId);
        }
        return post;
    }

    public PostSummaryResponse fromPostToPostSummaryResponse(
            Post post, PostMetaResponse postMeta, UserSummary userSummary) {
        return PostSummaryResponse.of(post, postMeta, userSummary);
    }

    public PostDetailResponse fromPostToPostDetailResponse(
            Post post,
            PostMetaResponse postMeta,
            List<String> imageUrlList,
            String username,
            List<String> tags,
            boolean liked,
            boolean bookmarked) {
        return PostDetailResponse.from(
                post, postMeta, imageUrlList, username, tags, liked, bookmarked);
    }

    public PostDetailResponse fromPostToPostDetailResponse(
            Post post,
            PostMetaResponse postMeta,
            List<String> imageUrlList,
            String username,
            List<String> tags) {
        return PostDetailResponse.from(post, postMeta, imageUrlList, username, tags, false, false);
    }

    public PostDetailResponse fromPostToPostDetailResponse(
            Post post, PostMetaResponse postMeta, List<String> imageUrlList, String username) {
        return PostDetailResponse.from(
                post, postMeta, imageUrlList, username, List.of(), false, false);
    }

    public PostPersistResponse fromPostToPostPersistResponse(Post post) {
        return PostPersistResponse.from(post);
    }
}
