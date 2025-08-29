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

    @Column(name = "nickname", nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserGrade grade = USER;

    public void update(String nickname, Integer level, UserGrade grade) {
        if (nickname != null && !nickname.isEmpty()) {
            this.nickname = nickname;
        }
        if (grade != null) {
            this.grade = grade;
        }
        if (level != null) {
            this.level = level;
        }
    }
}
