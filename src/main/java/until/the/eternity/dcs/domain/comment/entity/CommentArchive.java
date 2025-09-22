package until.the.eternity.dcs.domain.comment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import until.the.eternity.dcs.common.entity.AuditableEntity;

@Entity
@Table(name = "comment_archive")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentArchive extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId; // 대댓글의 경우 상위 댓글 ID

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_blocked")
    @Builder.Default
    private Boolean isBlocked = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt; // 이관 시간
}
