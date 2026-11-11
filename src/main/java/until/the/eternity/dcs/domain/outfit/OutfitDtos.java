package until.the.eternity.dcs.domain.outfit;

import java.time.Instant;
import java.util.List;

public final class OutfitDtos {
    private OutfitDtos() {}

    public record Item(
            String slot, String inputType, String name, String topCategory, String subCategory) {}

    public record MediaOrder(Long imageId, Integer uploadIndex) {}

    public record Write(
            String content, List<String> tags, List<Item> items, List<MediaOrder> mediaOrder) {}

    public record Author(
            long id,
            String nickname,
            String profileImageUrl,
            String serverName,
            boolean verified,
            String activityGrade) {
        public Author(
                long id,
                String nickname,
                String profileImageUrl,
                String serverName,
                boolean verified) {
            this(id, nickname, profileImageUrl, serverName, verified, null);
        }
    }

    public record Image(long id, String url, int displayOrder) {}

    public record Outfit(
            long id,
            long userId,
            Author author,
            String content,
            List<Image> images,
            List<Item> items,
            List<String> tags,
            long likeCount,
            long commentCount,
            long shareCount,
            Instant createdAt,
            Instant updatedAt) {}

    public record Page<T>(List<T> content, String nextCursor, boolean hasNext) {}

    public record ViewerState(long outfitId, boolean liked, boolean bookmarked) {}

    public record Comment(
            long id,
            long outfitId,
            long userId,
            Author author,
            String content,
            Long parentId,
            boolean deleted,
            long replyCount,
            Instant createdAt,
            Instant updatedAt) {}

    public record CommentWrite(String content, Long parentId) {}

    public record ShareWrite(String requestId, String channel) {}

    public record ShareResult(long shareCount) {}
}
