package until.the.eternity.dcs.domain.report.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportTargetType {
    POST("POST", "게시글"),
    COMMENT("COMMENT", "댓글"),
    USER("USER", "사용자");

    private final String code;
    private final String description;

    private static final Map<String, ReportTargetType> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(ReportTargetType::getCode, Function.identity()));

    public static Optional<ReportTargetType> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}
