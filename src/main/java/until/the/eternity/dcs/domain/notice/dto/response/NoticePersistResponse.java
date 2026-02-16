package until.the.eternity.dcs.domain.notice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.notice.entity.Notice;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record NoticePersistResponse(
        @Schema(description = "알림 아이디", example = "1", requiredMode = REQUIRED) Long id) {
    public static NoticePersistResponse from(Notice notice) {
        return NoticePersistResponse.builder().id(notice.getId()).build();
    }
}
