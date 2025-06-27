package until.the.eternity.dcs.domain.report.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    REPORTED("REPORTED", "신고 접수"),
    REVIEWING("REVIEWING", "검토 중"),
    REJECT("REJECT", "반려"),
    ACCEPT("ACCEPT", "승인"),
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