package until.the.eternity.dcs.domain.notice.dto.stream;

import until.the.eternity.dcs.domain.notice.enums.NoticeType;

public record NotificationJob(
        String notificationId, Long userId, String channel, NoticeType noticeType, String url) {}
