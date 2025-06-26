package until.the.eternity.dcs.domain.post.enums;

import lombok.Getter;

@Getter
public enum SearchType {
    TITLE("title", "제목"),
    CONTENT("content", "내용"),
    TITLE_CONTENT("title_content", "제목+내용"),
    AUTHOR("author", "작성자"),
    TAG("tag", "태그");

    private final String code;
    private final String description;

    SearchType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SearchType fromCode(String code) {
        for (SearchType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown search type code: " + code);
    }
}