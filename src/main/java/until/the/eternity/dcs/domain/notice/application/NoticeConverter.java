package until.the.eternity.dcs.domain.notice.application;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.dto.response.NoticeCommonResponse;
import until.the.eternity.dcs.domain.notice.dto.response.NoticePersistResponse;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

@Component
@RequiredArgsConstructor
public class NoticeConverter {

    public Notice fromSendRequest(NoticeSendRequest request) {
        NoticeType noticeType = request.noticeType();

        return Notice.builder()
                .userId(request.receiverId())
                .title(noticeType.getDescription())
                .noticeType(noticeType)
                .createdAt(LocalDateTime.now())
                .url(request.url())
                .isRead(false)
                .build();
    }

    public NoticeCommonResponse toNoticeCommonResponse(Notice notice) {
        return NoticeCommonResponse.from(notice);
    }

    public NoticePersistResponse toNoticePersistResponse(Notice notice) {
        return NoticePersistResponse.from(notice);
    }
}
