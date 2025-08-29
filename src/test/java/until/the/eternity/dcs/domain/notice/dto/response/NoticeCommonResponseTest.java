package until.the.eternity.dcs.domain.notice.dto.response;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

class NoticeCommonResponseTest {

    @Test
    void from_Success() {
        Notice notice =
                Notice.builder()
                        .id(1L)
                        .title("title")
                        .noticeType(NoticeType.ANNOUNCEMENT)
                        .url("test.com")
                        .createdAt(LocalDateTime.now())
                        .build();
        // todo
        //        NoticeCommonResponse noticeResponse = NoticeCommonResponse.from(notice);
        //
        //        assertThat(noticeResponse).isNotNull();
        //        assertThat(noticeResponse.title()).isEqualTo(notice.getTitle());
        //        assertThat(noticeResponse.url()).isEqualTo(notice.getUrl());
        //        assertThat(noticeResponse.createdAt()).isEqualTo(notice.getCreatedAt());
        //        assertThat(noticeResponse.contents()).isEqualTo("새로운 공지가 게시되었습니다.");
    }
}
