package until.the.eternity.dcs.domain.notice.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

@Builder
public record NoticeSendRequest(
        @Schema(description = "수신자 아이디", example = "1", requiredMode = REQUIRED) Long receiverId,
        @Schema(description = "알림 타입", example = "POST_LIKE", requiredMode = REQUIRED)
                NoticeType noticeType,
        @Schema(description = "알림이 발생하게 된 위치", example = "/api/posts/1", requiredMode = REQUIRED)
                String url,
        @Schema(description = "송신자 아이디", example = "1", requiredMode = REQUIRED) Long senderId) {

    public static NoticeSendRequest fromNotificationJob(NotificationJob job) {
        return NoticeSendRequest.builder()
                .receiverId(job.userId())
                .noticeType(job.noticeType())
                .url(job.url())
                .build();
    }
}
