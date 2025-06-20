package until.the.eternity.dcs.domain.report;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_type", nullable = false, length = 10)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_cd", nullable = false, length = 10)
    private String categoryCd;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status_cd", nullable = false, length = 10)
    @Builder.Default
    private String statusCd = "REPORTED";

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "replied_by")
    private Long repliedBy;

    @Column(name = "revived_at")
    private LocalDateTime revivedAt;

    @Column(name = "revived_by")
    private Long revivedBy;
}