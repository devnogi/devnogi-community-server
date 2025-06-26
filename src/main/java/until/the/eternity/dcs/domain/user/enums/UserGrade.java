package until.the.eternity.dcs.domain.user.enums;

import lombok.Getter;

@Getter
public enum UserGrade {
    USER("user", "일반 사용자"),
    ADMIN("admin", "관리자");

    private final String code;
    private final String description;

    UserGrade(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserGrade fromCode(String code) {
        for (UserGrade grade : values()) {
            if (grade.code.equals(code)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("Unknown user grade code: " + code);
    }
}