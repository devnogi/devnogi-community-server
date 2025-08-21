package until.the.eternity.dcs.domain.notice.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

public record NoticeSendRequest(
        @Schema(description = "수신자 아이디", example = "1", requiredMode = REQUIRED) Long userId,
        @Schema(description = "알림 타입", example = "POST_LIKE", requiredMode = REQUIRED)
                NoticeType noticeType,
        @Schema(description = "알림이 발생하게 된 위치", example = "/api/posts/1", requiredMode = REQUIRED)
                String url) {}
