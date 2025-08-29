package until.the.eternity.dcs.domain.notice.application;

import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final NoticeUserConverter noticeUserConverter;

    @Transactional
    public NoticePersistResponse createNotice(NoticeSendRequest request) {
        authCheck(request.noticeType());

        if (request.receiverId().equals(0L)) {
            return broadcastNotice(request);
        }

        Notice notice = noticeConverter.fromSendRequest(request);
        Notice save = noticeRepository.save(notice);
        NoticeUser noticeUser = noticeUserConverter.fromSendRequest(request, save.getId());
        noticeUserRepository.save(noticeUser);

        return noticeConverter.toNoticePersistResponse(save);
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

    public List<NoticeCommonResponse> getNoticeList(Long userId, Integer day) {
        // 최근 day일 데이터
        LocalDateTime date = LocalDateTime.now().minusDays(day).toLocalDate().atStartOfDay();
        List<NoticeUser> myNoticeList = noticeUserRepository.findByCreatedAtAndUserId(date, userId);

        List<Notice> noticeList =
                noticeRepository.findByIdIn(
                        myNoticeList.stream().map(NoticeUser::getNoticeId).toList());

        Map<Long, NoticeUser> map = new HashMap<>();
        for (NoticeUser noticeUser : myNoticeList) {
            map.put(noticeUser.getNoticeId(), noticeUser);
        }

        return noticeList.stream()
                .map(
                        notice ->
                                noticeConverter.toNoticeCommonResponse(
                                        notice, map.get(notice.getId())))
                .toList();
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
