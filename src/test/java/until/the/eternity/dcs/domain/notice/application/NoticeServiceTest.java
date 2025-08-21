package until.the.eternity.dcs.domain.notice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_LIKE;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.dto.response.NoticePersistResponse;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.entity.NoticeRepository;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

class NoticeServiceTest {
    NoticeRepository noticeRepository = mock(NoticeRepository.class);
    NoticeConverter noticeConverter = new NoticeConverter();

    NoticeService noticeService;

    Long id = 1L;
    NoticeType noticeType = POST_LIKE;
    String url = "api/posts/1";
    Notice notice;

    @BeforeEach
    void init() {
        noticeService = new NoticeService(noticeRepository, noticeConverter);
        notice =
                Notice.builder()
                        .id(id)
                        .userId(id)
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
        Mockito.when(noticeRepository.save(Mockito.any(Notice.class))).thenReturn(notice);
        NoticeSendRequest request = new NoticeSendRequest(id, noticeType, url);

        // when
        NoticePersistResponse notice = noticeService.createNotice(request);

        // then
        assertThat(notice).isNotNull();
        assertThat(notice.id()).isEqualTo(id);
    }
}
