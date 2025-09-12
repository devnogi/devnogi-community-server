package until.the.eternity.dcs.domain.notice.application;

import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.exception.UserNotFoundException;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeConverter noticeConverter;
    private final NoticeUserRepository noticeUserRepository;
    private final NoticeUserConverter noticeUserConverter;
    private final UserSummaryRepository userSummaryRepository;
    private final NoticePermissionEvaluator noticePermissionEvaluator;

    @Transactional
    public NoticePersistResponse createNotice(NoticeSendRequest request) {
        authCheck(request.noticeType(), request.senderId());

        Notice notice = noticeRepository.save(noticeConverter.fromSendRequest(request));

        // 0L로 전송시 모든 유저와 연결
        if (request.receiverId().equals(0L)) {
            connectEveryUser(notice.getId());
        } else {
            connectToUser(notice.getId(), request.receiverId());
        }

        return noticeConverter.toNoticePersistResponse(notice);
    }

    @Transactional
    @PreAuthorize("@noticePermissionEvaluator.canRead(authentication)")
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

    @PreAuthorize("@noticePermissionEvaluator.canRead(authentication)")
    public List<NoticeCommonResponse> getNoticeList(Long userId, Integer day) {
        // 최근 day일 데이터
        LocalDateTime date = LocalDateTime.now().minusDays(day).toLocalDate().atStartOfDay();
        List<NoticeUser> myNoticeList = noticeUserRepository.findByCreatedAtAndUserId(date, userId);

        List<Long> idList = myNoticeList.stream().map(NoticeUser::getNoticeId).toList();
        List<Notice> noticeList = noticeRepository.findByIdIn(idList);

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

    // todo 배치 or DBMS에서 생성하도록 수정 필요
    @Transactional
    public void connectEveryUser(Long noticeId) {
        UserSummary user =
                userSummaryRepository
                        .findFirstByOrderByIdDesc()
                        .orElseThrow(UserNotFoundException::new);
        Long lastUserId = user.getId();

        for (long i = 1; i <= lastUserId; i++) {
            noticeUserRepository.save(noticeUserConverter.fromNoticeAndReceiverId(noticeId, i));
        }
    }

    @Transactional
    public void connectToUser(Long noticeId, Long receiverId) {
        NoticeUser noticeUser = noticeUserConverter.fromNoticeAndReceiverId(noticeId, receiverId);
        noticeUserRepository.save(noticeUser);
    }

    /** 수동 알림 전송일 경우 관리자인지 확인 */
    private void authCheck(NoticeType noticeType, Long userId) {
        if (noticeType.isManualNotice()) {
            UserSummary userSummary =
                    userSummaryRepository
                            .findById(userId)
                            .orElseThrow(() -> new UserNotFoundException(userId));
            if (userSummary.getGrade() != ADMIN) {
                throw new NoticeSendForbiddenException();
            }
        }
    }
}
