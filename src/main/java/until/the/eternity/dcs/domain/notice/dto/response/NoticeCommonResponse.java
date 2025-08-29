package until.the.eternity.dcs.domain.notice.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.entity.NoticeUser;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

@Builder
public record NoticeCommonResponse(
        @Schema(description = "알림 아이디", example = "1", requiredMode = REQUIRED) Long id,
        @Schema(description = "수신자 아이디", example = "1", requiredMode = REQUIRED) Long userId,
        @Schema(description = "알림 제목", example = "게시글 좋아요", requiredMode = REQUIRED) String title,
        @Schema(
                        description = "알림 내용",
                        example = "회원님의 게시글에 좋아요가 달렸습니다.",
                        requiredMode = NOT_REQUIRED)
                String contents,
        @Schema(description = "알림이 발생하게 된 위치", example = "/api/posts/1", requiredMode = REQUIRED)
                String url,
        @Schema(description = "알림 발생 시각", requiredMode = REQUIRED)
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime createdAt,
        @Schema(description = "알림 확인 여부", example = "false", requiredMode = REQUIRED)
                Boolean isRead) {
    public static NoticeCommonResponse from(Notice notice, NoticeUser noticeUser) {
        return NoticeCommonResponse.builder()
                .id(notice.getId())
                .userId(noticeUser.getUserId())
                .title(notice.getTitle())
                .contents(buildContents(notice.getNoticeType()))
                .url(notice.getUrl())
                .createdAt(notice.getCreatedAt())
                .isRead(noticeUser.getIsRead())
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
