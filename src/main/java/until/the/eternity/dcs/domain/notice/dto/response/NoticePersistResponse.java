package until.the.eternity.dcs.domain.notice.dto.response;

import lombok.Builder;
import until.the.eternity.dcs.domain.notice.entity.Notice;

@Builder
public record NoticePersistResponse(Long id) {
    public static NoticePersistResponse from(Notice notice) {
        return NoticePersistResponse.builder().id(notice.getId()).build();
    }
}
