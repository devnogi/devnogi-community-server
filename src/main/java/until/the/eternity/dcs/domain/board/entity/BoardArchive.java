package until.the.eternity.dcs.domain.board.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import until.the.eternity.dcs.common.entity.AuditableEntity;

@Entity
@Table(name = "board_archive")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardArchive extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "top_category", length = 50)
    private String topCategory;

    @Column(name = "sub_category", length = 50)
    private String subCategory;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;
}
