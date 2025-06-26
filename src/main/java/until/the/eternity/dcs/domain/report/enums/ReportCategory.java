package until.the.eternity.dcs.domain.report.enums;


import lombok.Getter;

@Getter
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

    ReportCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReportCategory fromCode(String code) {
        for (ReportCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown report category code: " + code);
    }
}