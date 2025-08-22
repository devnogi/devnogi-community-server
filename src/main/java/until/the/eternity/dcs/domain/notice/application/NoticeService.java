package until.the.eternity.dcs.domain.notice.application;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.dto.response.NoticeCommonResponse;
import until.the.eternity.dcs.domain.notice.dto.response.NoticePersistResponse;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.entity.NoticeRepository;
import until.the.eternity.dcs.domain.notice.exception.NoticeNotFoundException;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeConverter noticeConverter;

    public NoticePersistResponse createNotice(NoticeSendRequest noticeSendRequest) {
        Notice notice = noticeConverter.fromSendRequest(noticeSendRequest);

        return noticeConverter.toNoticePersistResponse(noticeRepository.save(notice));
    }

    @Transactional
    public NoticeCommonResponse getDetailNotice(Long id) {
        Notice notice =
                noticeRepository.findById(id).orElseThrow(() -> new NoticeNotFoundException(id));

        notice.read();

        return noticeConverter.toNoticeCommonResponse(notice);
    }

    public List<NoticeCommonResponse> getNoticeList(Integer day) {
        // 최근 day일 데이터
        LocalDateTime date = LocalDateTime.now().minusDays(day).toLocalDate().atStartOfDay();

        List<Notice> noticeList = noticeRepository.findByCreatedAt(date);

        return noticeList.stream().map(noticeConverter::toNoticeCommonResponse).toList();
    }
}
