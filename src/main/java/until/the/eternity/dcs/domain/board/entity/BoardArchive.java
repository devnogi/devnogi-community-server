package until.the.eternity.dcs.domain.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "board_archive")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardArchive {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedDate
    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;

    public static BoardArchive from(Board board) {
        return BoardArchive.builder()
                .boardId(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .topCategory(board.getTopCategory())
                .subCategory(board.getSubCategory())
                .createdBy(board.getCreatedBy())
                .createdAt(board.getCreatedAt())
                .updatedBy(board.getUpdatedBy())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}
