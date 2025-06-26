package until.the.eternity.dcs.domain.report.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {
    REPORTED("REPORTED", "신고 접수"),
    REVIEWING("REVIEWING", "검토 중"),
    REJECT("REJECT", "반려"),
    ACCEPT("ACCEPT", "승인"),
    RESOLVED("RESOLVED", "처리 완료");

    private final String code;
    private final String description;

    ReportStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReportStatus fromCode(String code) {
        for (ReportStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown report status code: " + code);
    }
}