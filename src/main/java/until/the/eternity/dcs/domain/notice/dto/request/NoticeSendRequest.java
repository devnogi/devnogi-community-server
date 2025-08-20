package until.the.eternity.dcs.domain.notice.dto.request;

import until.the.eternity.dcs.domain.notice.enums.NoticeType;

public record NoticeSendRequest(Long userId, NoticeType noticeType, String url) {}
