package until.the.eternity.dcs.domain.announcement.entity;

import jakarta.persistence.*;
import lombok.*;
import until.the.eternity.dcs.common.entity.AuditableEntity;
import until.the.eternity.dcs.domain.board.entity.Board;

@Entity
@Table(name = "announcement")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "is_draft", nullable = false)
    @Builder.Default
    private Boolean isDraft = false;

    @Column(name = "is_global")
    @Builder.Default
    private Boolean isGlobal = false;
}
