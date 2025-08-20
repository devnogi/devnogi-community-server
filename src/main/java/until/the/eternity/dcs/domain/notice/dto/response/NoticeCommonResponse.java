package until.the.eternity.dcs.domain.notice.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

@Builder
public record NoticeCommonResponse(
        Long id,
        Long userId,
        String title,
        String contents,
        String url,
        LocalDateTime createdAt,
        Boolean isRead) {
    public static NoticeCommonResponse from(Notice notice) {
        return NoticeCommonResponse.builder()
                .id(notice.getId())
                .userId(notice.getUserId())
                .title(notice.getTitle())
                .contents(buildContents(notice.getNoticeType()))
                .url(notice.getUrl())
                .createdAt(notice.getCreatedAt())
                .isRead(notice.getIsRead())
                .build();
    }

    private static String buildContents(NoticeType noticeType) {
        return switch (noticeType) {
            case POST_LIKE -> "회원님의 게시글에 좋아요가 달렸습니다.";
            case POST_COMMENT -> "회원님의 게시글에 댓글이 달렸습니다.";
            case COMMENT_REPLY -> "회원님의 댓글에 답글이 달렸습니다.";
            case COMMENT_LIKE -> "회원님의 댓글에 좋아요가 달렸습니다.";
            case ANNOUNCEMENT -> "새로운 공지가 게시되었습니다.";
            case SYSTEM -> "시스템 알림입니다.";
            case EVENT -> "새로운 이벤트가 게시되었습니다.";
            case REPORT_RESULT -> "회원님의 신고가 처리되었습니다.";
            case POST_BLOCKED -> "회원님의 게시글이 제재되었습니다.";
            case COMMENT_BLOCKED -> "회원님의 댓글이 제재되었습니다.";
        };
    }
}
