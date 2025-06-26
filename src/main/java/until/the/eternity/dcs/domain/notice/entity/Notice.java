package until.the.eternity.dcs.domain.notice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 25)
    private String title;

    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "URL", nullable = false, columnDefinition = "TEXT")
    private String url;
}