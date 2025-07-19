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
public enum ReportStatus {
    REPORTED("REPORTED", "신고 접수"),
    REJECT("REJECT", "반려"),
    ACCEPT("ACCEPT", "승인"),
    // todo 아래 두 개를 사용해야될지 고민입니다. (기존 ddl엔 위 세 개만 있습니다)
    REVIEWING("REVIEWING", "검토 중"),
    RESOLVED("RESOLVED", "처리 완료");

    private final String code;
    private final String description;

    private static final Map<String, ReportStatus> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(ReportStatus::getCode, Function.identity()));

    public static Optional<ReportStatus> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}
