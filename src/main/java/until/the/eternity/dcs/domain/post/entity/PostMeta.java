package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import lombok.*;

@ToString
@Entity
@Table(name = "post_meta")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMeta {

    @Id private Long postId;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    public static PostMeta create(Long postId, Integer commentCount) {
        return PostMeta.builder()
                .postId(postId)
                .viewCount(0)
                .likeCount(0)
                .commentCount(commentCount)
                .build();
    }

    public void like() {
        this.likeCount++;
    }

    public void unlike() {
        this.likeCount--;
    }

    public void viewPost() {
        this.viewCount++;
    }

    public void addComment() {
        this.commentCount++;
    }

    public void deleteComment() {
        this.commentCount--;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("likeCount", likeCount);
        map.put("viewCount", viewCount);
        map.put("commentCount", commentCount);
        return map;
    }
}
