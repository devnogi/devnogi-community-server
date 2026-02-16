package until.the.eternity.dcs.domain.notice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum NoticeType {
    POST_LIKE("POST_LIKE", "게시글 좋아요"),
    POST_COMMENT("POST_COMMENT", "게시글 댓글"),
    COMMENT_REPLY("COMMENT_REPLY", "댓글 답글"),
    COMMENT_LIKE("COMMENT_LIKE", "댓글 좋아요"),
    ANNOUNCEMENT("ANNOUNCEMENT", "공지사항"),
    SYSTEM("SYSTEM", "시스템 알림"),
    EVENT("EVENT", "이벤트 알림"),
    REPORT_RESULT("REPORT_RESULT", "신고 처리 결과"),
    POST_BLOCKED("POST_BLOCKED", "게시글 제재"),
    COMMENT_BLOCKED("COMMENT_BLOCKED", "댓글 제재");

    private final String code;
    private final String description;

    private static final Map<String, NoticeType> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(NoticeType::getCode, Function.identity()));

    public static Optional<NoticeType> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    /** 관리자의 수동 알림인지 확인 */
    public Boolean isManualNotice() {
        return this == EVENT;
    }
}
