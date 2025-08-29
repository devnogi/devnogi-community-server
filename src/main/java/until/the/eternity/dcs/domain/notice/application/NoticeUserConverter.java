package until.the.eternity.dcs.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.entity.NoticeUser;

@Component
@RequiredArgsConstructor
public class NoticeUserConverter {

    public NoticeUser fromSendRequest(NoticeSendRequest request, Long noticeId) {
        return NoticeUser.builder()
                .noticeId(noticeId)
                .userId(request.receiverId())
                .isRead(false)
                .build();
    }
}
