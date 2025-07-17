package until.the.eternity.dcs.domain.post.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    private static final Map<String, PostSortType> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(PostSortType::getCode, Function.identity()));

    public static Optional<PostSortType> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}
