package until.the.eternity.dcs.domain.board.enums;

import lombok.Getter;

@Getter
public enum BoardSubCategory {
    // 일반 하위 카테고리
    GENERAL_DISCUSSION("general_discussion", "일반 토론"),
    GENERAL_INFO("general_info", "정보 공유"),

    // 공략 하위 카테고리
    BEGINNER_GUIDE("beginner_guide", "초보자 가이드"),
    ADVANCED_GUIDE("advanced_guide", "고급 가이드"),
    TIPS("tips", "팁과 노하우"),

    // 거래 하위 카테고리
    ITEM_TRADE("item_trade", "아이템 거래"),
    ACCOUNT_TRADE("account_trade", "계정 거래"), //다소 권장되지 않을거같긴한데
    CURRENCY_TRADE("currency_trade", "재화 거래"),

    // 질문답변 하위 카테고리
    TECHNICAL_QNA("technical_qna", "기술 문의"),
    GAME_QNA("game_qna", "게임 문의"),
    GENERAL_QNA("general_qna", "일반 문의");

    private final String code;
    private final String description;

    BoardSubCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BoardSubCategory fromCode(String code) {
        for (BoardSubCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown board sub category code: " + code);
    }
}