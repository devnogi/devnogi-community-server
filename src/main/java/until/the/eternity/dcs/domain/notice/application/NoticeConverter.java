package until.the.eternity.dcs.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.dto.response.NoticeCommonResponse;
import until.the.eternity.dcs.domain.notice.dto.response.NoticePersistResponse;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.entity.NoticeUser;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NoticeConverter {

    public Notice fromSendRequest(NoticeSendRequest request) {
        NoticeType noticeType = request.noticeType();

        return Notice.builder()
                .title(noticeType.getDescription())
                .noticeType(noticeType)
                .createdAt(LocalDateTime.now())
                .url(request.url())
                .build();
    }

    public NoticeCommonResponse toNoticeCommonResponse(
            Notice notice, NoticeUser noticeUser, String username) {
        return NoticeCommonResponse.from(notice, noticeUser, username);
    }

    public NoticePersistResponse toNoticePersistResponse(Notice notice) {
        return NoticePersistResponse.from(notice);
    }
}
