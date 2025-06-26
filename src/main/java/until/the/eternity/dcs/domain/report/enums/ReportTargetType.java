package until.the.eternity.dcs.domain.report.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportTargetType {
    POST("post", "게시글"),
    COMMENT("comment", "댓글"),
    USER("user", "사용자");

    private final String code;
    private final String description;

    public static ReportTargetType fromCode(String code) {
        for (ReportTargetType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown report target type code: " + code);
    }
}