package until.the.eternity.dcs.domain.board.enums;

import lombok.Getter;

@Getter
public enum BoardTopCategory {
    GENERAL("general", "일반"), //일반 게시판?
    STRATEGY("strategy", "공략"),
    TRADE("trade", "거래"),
    QNA("qna", "질문답변"),
    EVENT("event", "이벤트"),
    NOTICE("notice", "공지사항"),
    FREE("free", "자유게시판");

    private final String code;
    private final String description;

    BoardTopCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BoardTopCategory fromCode(String code) {
        for (BoardTopCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown board top category code: " + code);
    }
}