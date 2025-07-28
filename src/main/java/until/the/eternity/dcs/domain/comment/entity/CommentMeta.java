package until.the.eternity.dcs.domain.comment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "comment_meta")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CommentMeta {
    @Id private Long commentId;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    public static CommentMeta create(Long commentId) {
        return CommentMeta.builder().commentId(commentId).likeCount(0).build();
    }

    public void like() {
        this.likeCount++;
    }

    public void unlike() {
        this.likeCount--;
    }
}
