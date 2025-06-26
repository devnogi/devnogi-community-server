package until.the.eternity.dcs.domain.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostSortType {
    LATEST("latest", "최신순"),
    MOST_VIEWED("most_viewed", "조회수순"),
    MOST_LIKED("most_liked", "좋아요순"),
    MOST_COMMENTED("most_commented", "댓글순");

    private final String code;
    private final String description;

    public static PostSortType fromCode(String code) {
        for (PostSortType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown post sort type code: " + code);
    }
}