package until.the.eternity.dcs.domain.user.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserGrade {
    USER("user", "일반 사용자"),
    ADMIN("admin", "관리자");

    private final String code;
    private final String description;

    private static final Map<String, UserGrade> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(UserGrade::getCode, Function.identity()));

    public static Optional<UserGrade> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}
