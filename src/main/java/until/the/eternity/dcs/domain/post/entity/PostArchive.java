package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import until.the.eternity.dcs.domain.post.enums.PostContentFormat;

@Entity
@Table(name = "post_archive")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostArchive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "plain_content", columnDefinition = "TEXT")
    private String plainContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_format", nullable = false, length = 32)
    private PostContentFormat contentFormat;

    @Column(name = "is_draft", nullable = false)
    @Builder.Default
    private boolean isDraft = false;

    @Column(name = "is_blocked")
    @Builder.Default
    private boolean isBlocked = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedDate
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    public static PostArchive from(Post post) {
        return PostArchive.builder()
                .postId(post.getId())
                .boardId(post.getBoard() == null ? null : post.getBoard().getId())
                .userId(post.getUserId())
                .title(post.getTitle())
                .content(post.getContent())
                .plainContent(post.getPlainContent())
                .contentFormat(post.getContentFormat())
                .isDraft(post.getIsDraft())
                .isBlocked(post.getIsBlocked())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .updatedBy(post.getUpdatedBy())
                .build();
    }
}
