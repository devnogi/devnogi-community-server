package until.the.eternity.dcs.domain.notice.application;

import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

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
import until.the.eternity.dcs.domain.notice.entity.NoticeUser;
import until.the.eternity.dcs.domain.notice.entity.NoticeUserRepository;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;
import until.the.eternity.dcs.domain.notice.exception.NoticeNotFoundException;
import until.the.eternity.dcs.domain.notice.exception.NoticeSendForbiddenException;
import until.the.eternity.dcs.domain.user.application.UserService;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeConverter noticeConverter;
    private final UserService userService;
    private final NoticeUserRepository noticeUserRepository;

    @Transactional
    public NoticePersistResponse createNotice(NoticeSendRequest request) {
        authCheck(request.noticeType());

        if (request.receiverId().equals(0L)) {
            return broadcastNotice(request);
        }

        Notice notice = noticeConverter.fromSendRequest(request);
        // todo noticeuser 추가

        return noticeConverter.toNoticePersistResponse(noticeRepository.save(notice));
    }

    @Transactional
    public NoticeCommonResponse getDetailNotice(Long id) {
        Notice notice =
                noticeRepository.findById(id).orElseThrow(() -> new NoticeNotFoundException(id));

        NoticeUser noticeUser =
                noticeUserRepository
                        .findByNoticeId(id)
                        .orElseThrow(() -> new NoticeNotFoundException(id));
        noticeUser.read();

        return noticeConverter.toNoticeCommonResponse(notice, noticeUser);
    }

    public List<NoticeCommonResponse> getNoticeList(Integer day) {
        // 최근 day일 데이터
        LocalDateTime date = LocalDateTime.now().minusDays(day).toLocalDate().atStartOfDay();

        List<Notice> noticeList = noticeRepository.findByCreatedAt(date);

        // todo
        //		return noticeList.stream().map(noticeConverter::toNoticeCommonResponse).toList();
        return null;
    }

    /** 수동 알림 전송일 경우 관리자인지 확인 */
    private void authCheck(NoticeType noticeType) {
        if (noticeType.isManualNotice()) {
            if (userService.getCurrentUser().getGrade() != ADMIN) {
                throw new NoticeSendForbiddenException();
            }
        }
    }

    // todo
    private NoticePersistResponse broadcastNotice(NoticeSendRequest request) {
        return null;
    }
}
