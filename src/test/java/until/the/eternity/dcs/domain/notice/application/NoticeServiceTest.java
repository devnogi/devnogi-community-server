package until.the.eternity.dcs.domain.notice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_LIKE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.dto.response.NoticeCommonResponse;
import until.the.eternity.dcs.domain.notice.dto.response.NoticePersistResponse;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.entity.NoticeRepository;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;
import until.the.eternity.dcs.domain.notice.exception.NoticeNotFoundException;
import until.the.eternity.dcs.domain.user.application.UserService;

class NoticeServiceTest {
    NoticeRepository noticeRepository = mock(NoticeRepository.class);
    UserService userService = mock(UserService.class);
    NoticeConverter noticeConverter = new NoticeConverter();

    NoticeService noticeService;

    Long id = 1L;
    Long userId = 1L;
    NoticeType noticeType = POST_LIKE;
    String url = "api/posts/1";
    Notice notice;

    @BeforeEach
    void init() {
        noticeService = new NoticeService(noticeRepository, noticeConverter, userService);
        notice =
                Notice.builder()
                        .id(id)
                        .userId(userId)
                        .title(noticeType.getDescription())
                        .noticeType(noticeType)
                        .createdAt(LocalDateTime.now())
                        .url(url)
                        .isRead(false)
                        .build();
    }

    @Test
    @DisplayName("createNotice는 notice를 생성 저장한다.")
    void createNotice_Success() {
        // given
        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);
        NoticeSendRequest request = new NoticeSendRequest(id, noticeType, url, userId);

        // when
        NoticePersistResponse notice = noticeService.createNotice(request);

        // then
        assertThat(notice).isNotNull();
        assertThat(notice.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("getDetailNotice는 notice를 아이디로 단건 조회한다.")
    void getDetailNotice_Success() {
        // given
        when(noticeRepository.findById(id)).thenReturn(Optional.of(notice));

        // when
        NoticeCommonResponse response = noticeService.getDetailNotice(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(notice.getId());
        assertThat(response.userId()).isEqualTo(notice.getUserId());
        assertThat(response.title()).isEqualTo(notice.getTitle());
        assertThat(response.contents()).isEqualTo("회원님의 게시글에 좋아요가 달렸습니다.");
        assertThat(response.url()).isEqualTo(notice.getUrl());
        assertThat(response.createdAt()).isEqualTo(notice.getCreatedAt());
        assertThat(response.isRead()).isEqualTo(notice.getIsRead());
    }

    @Test
    @DisplayName("getDetailNotice는 아이디가 존재하지 않을 때 NoticeNotFoundException를 반환한다.")
    void getDetailNotice_NoticeNotFoundException() {
        // given
        when(noticeRepository.findById(id)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> noticeService.getDetailNotice(id))
                .isInstanceOf(NoticeNotFoundException.class);
    }

    @Test
    @DisplayName("getNoticeList는 day일간의 notice를 리스트로 조회한다.")
    void getNoticeList_Success() {
        // given
        int day = 1;
        when(noticeRepository.findByCreatedAt(any())).thenReturn(List.of(notice));

        // when
        List<NoticeCommonResponse> response = noticeService.getNoticeList(day);

        // then
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0).id()).isEqualTo(id);
    }
}
