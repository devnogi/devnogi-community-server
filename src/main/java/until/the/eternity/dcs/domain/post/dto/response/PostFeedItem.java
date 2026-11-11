package until.the.eternity.dcs.domain.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostFeedItem(
        Long id,
        Long userId,
        Author author,
        String content,
        List<Media> media,
        int viewCount,
        int likeCount,
        int commentCount,
        boolean liked,
        boolean bookmarked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public record Author(
            String nickname,
            String profileImageUrl,
            String serverName,
            boolean verified,
            String activityGrade) {
        public Author(
                String nickname, String profileImageUrl, String serverName, boolean verified) {
            this(nickname, profileImageUrl, serverName, verified, null);
        }
    }

    public record Media(Long id, String type, String url, String contentType, int order) {}
}
