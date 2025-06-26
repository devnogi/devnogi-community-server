package until.the.eternity.dcs.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserGrade {
    USER("user", "일반 사용자"),
    ADMIN("admin", "관리자");

    private final String code;
    private final String description;

    public static UserGrade fromCode(String code) {  //TODO: 에러 발생을 해버릴지 아니면 optional 객체로 처리할지 고민
        for (UserGrade grade : values()) {
            if (grade.code.equals(code)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("Unknown user grade code: " + code);
    }
}