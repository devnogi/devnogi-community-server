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
public enum PostMetaType {
    VIEW("view", "조회"),
    LIKE("like", "좋아요"),
    UNLIKE("unlike", "종하요 취소"),
    ADD_COMMENT("addComment", "댓글 작성"),
    DELETE_COMMENT("deleteComment", "댓글 삭제");

    private final String code;
    private final String description;

    private static final Map<String, PostMetaType> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(PostMetaType::getCode, Function.identity()));

    public static Optional<PostMetaType> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}
