package until.the.eternity.dcs.domain.comment.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import until.the.eternity.dcs.common.entity.SoftDeleteEntity;
import until.the.eternity.dcs.domain.post.entity.Post;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(
            mappedBy = "parentComment",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private List<Comment> childComments;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_blocked")
    @Builder.Default
    private Boolean isBlocked = false;

    public void update(String content, Long userId) {
        this.content = content;
        this.setUpdatedBy(userId);
    }

    public void disconnectWithPost() {
        this.post = null;
    }
}
