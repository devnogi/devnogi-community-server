package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import until.the.eternity.dcs.common.entity.AuditableEntity;

@Entity
@Table(name = "post_archive")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostArchive extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_draft", nullable = false)
    @Builder.Default
    private boolean isDraft = false;

    @Column(name = "is_blocked")
    @Builder.Default
    private boolean isBlocked = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
}
