package until.the.eternity.dcs.domain.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum SearchType {
    TITLE("title", "제목"),
    CONTENT("content", "내용"),
    TITLE_CONTENT("title_content", "제목+내용"),
    AUTHOR("author", "작성자"),
    TAG("tag", "태그");

    private final String code;
    private final String description;

    private static final Map<String, SearchType> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(SearchType::getCode, Function.identity()));

    public static Optional<SearchType> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}
