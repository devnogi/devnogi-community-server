package until.the.eternity.dcs.domain.report.entitiy;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import until.the.eternity.dcs.domain.report.enums.ReportCategory;
import until.the.eternity.dcs.domain.report.enums.ReportStatus;
import until.the.eternity.dcs.domain.report.enums.ReportTargetType;

import java.time.LocalDateTime;

import static until.the.eternity.dcs.domain.report.enums.ReportStatus.REPORTED;

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

    @Enumerated(EnumType.STRING)
    private ReportTargetType categoryCd;

    @Enumerated(EnumType.STRING)
    private ReportCategory reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus statusCd = REPORTED;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "replied_by")
    private Long repliedBy;

    @Column(name = "revived_at")
    private LocalDateTime revivedAt;

    @Column(name = "revived_by")
    private Long revivedBy;
}