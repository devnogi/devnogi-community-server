package until.the.eternity.dcs.domain.post.dto.response;

import lombok.Builder;
import until.the.eternity.dcs.domain.post.entity.PostMeta;

@Builder
public record PostMetaResponse(
        Long postId, Integer viewCount, Integer likeCount, Integer commentCount) {
    public static PostMetaResponse of(
            PostMeta postMeta, int viewsToAdd, int likesToAdd, int commentsToAdd) {
        return PostMetaResponse.builder()
                .postId(postMeta.getPostId())
                .viewCount(postMeta.getViewCount() + viewsToAdd)
                .likeCount(postMeta.getLikeCount() + likesToAdd)
                .commentCount(postMeta.getCommentCount() + commentsToAdd)
                .build();
    }

    public static PostMetaResponse from(PostMeta postMeta) {
        return PostMetaResponse.builder()
                .postId(postMeta.getPostId())
                .viewCount(postMeta.getViewCount())
                .likeCount(postMeta.getLikeCount())
                .commentCount(postMeta.getCommentCount())
                .build();
    }
}
