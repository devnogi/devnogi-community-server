package until.the.eternity.dcs.domain.notice.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

class CommonNoticeResponseTest {

    @Test
    void from_Success() {
        Notice notice =
                Notice.builder()
                        .id(1L)
                        .title("title")
                        .noticeType(NoticeType.ANNOUNCEMENT)
                        .url("test.com")
                        .createdAt(LocalDateTime.now())
                        .isRead(false)
                        .build();

        CommonNoticeResponse noticeResponse = CommonNoticeResponse.from(notice);

        assertThat(noticeResponse).isNotNull();
        assertThat(noticeResponse.title()).isEqualTo(notice.getTitle());
        assertThat(noticeResponse.url()).isEqualTo(notice.getUrl());
        assertThat(noticeResponse.createdAt()).isEqualTo(notice.getCreatedAt());
        assertThat(noticeResponse.isRead()).isEqualTo(notice.getIsRead());
        assertThat(noticeResponse.contents()).isEqualTo("새로운 공지가 게시되었습니다.");
    }
}
