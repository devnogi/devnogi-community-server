package until.the.eternity.dcs.domain.announcement.dto.response;

import lombok.Builder;

@Builder
public record AnnouncementToggleResponse(Long id, Boolean isGlobal) {}
