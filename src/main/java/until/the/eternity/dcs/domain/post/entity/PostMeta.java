package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_meta")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMeta {

    @Id private Long postId;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    public static PostMeta create(Long postId) {
        return PostMeta.builder().postId(postId).viewCount(0).likeCount(0).commentCount(0).build();
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
}
