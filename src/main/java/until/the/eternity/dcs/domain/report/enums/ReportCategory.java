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
public enum ReportCategory {
    SPAM("SPAM", "스팸/도배"),
    ABUSE("ABUSE", "욕설/비방"),
    ADULT("ADULT", "성인/선정성"),
    VIOLENCE("VIOLENCE", "폭력적 내용"),
    FRAUD("FRAUD", "사기/사칭"),
    COPYRIGHT("COPYRIGHT", "저작권 침해"),
    PRIVACY("PRIVACY", "개인정보 노출"),
    ILLEGAL("ILLEGAL", "불법 정보"),
    ADVERTISEMENT("ADVERTISEMENT", "무단 광고"),
    OTHER("OTHER", "기타");

    private final String code;
    private final String description;

    private static final Map<String, ReportCategory> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(ReportCategory::getCode, Function.identity()));


    public static Optional<ReportCategory> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}