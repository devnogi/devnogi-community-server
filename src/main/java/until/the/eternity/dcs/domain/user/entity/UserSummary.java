package until.the.eternity.dcs.domain.user.entity;

import static until.the.eternity.dcs.domain.user.enums.UserGrade.USER;

import jakarta.persistence.*;
import lombok.*;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

@Entity
@Table(name = "user_summary")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummary {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    @Column(name = "server_name", length = 50)
    private String serverName;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "last_auth_sync_revision")
    private Long lastAuthSyncRevision;

    @Column(name = "activity_grade", length = 32)
    private String activityGrade;

    public String getActivityGrade() {
        return verified ? activityGrade : null;
    }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserGrade grade = USER;

    public void update(String nickname, Integer level, String serverName, UserGrade grade) {
        if (nickname != null && !nickname.isEmpty()) {
            this.nickname = nickname;
        }
        if (grade != null) {
            this.grade = grade;
        }
        if (serverName != null && !serverName.isEmpty()) {
            this.serverName = serverName;
        }
        if (level != null) {
            this.level = level;
        }
    }
}
