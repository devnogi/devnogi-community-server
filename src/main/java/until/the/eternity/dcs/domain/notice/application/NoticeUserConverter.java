package until.the.eternity.dcs.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.notice.entity.NoticeUser;

@Component
@RequiredArgsConstructor
public class NoticeUserConverter {

    public NoticeUser fromNoticeAndReceiverId(Long noticeId, Long receiverId) {
        return NoticeUser.builder().noticeId(noticeId).userId(receiverId).isRead(false).build();
    }
}
