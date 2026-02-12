package until.the.eternity.dcs.domain.notice.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.entity.NoticeUser;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

class NoticeCommonResponseTest {

    @Test
    void from_Success() {
        String username = "username";
        Notice notice =
                Notice.builder()
                        .id(1L)
                        .title("title")
                        .noticeType(NoticeType.ANNOUNCEMENT)
                        .url("test.com")
                        .createdAt(LocalDateTime.now())
                        .build();

        NoticeUser noticeUser =
                NoticeUser.builder()
                        .noticeId(1L)
                        .userId(1L)
                        .isRead(false)
                        .createdAt(notice.getCreatedAt())
                        .build();

        NoticeCommonResponse noticeResponse =
                NoticeCommonResponse.from(notice, noticeUser, username);

        assertThat(noticeResponse).isNotNull();
        assertThat(noticeResponse.title()).isEqualTo(notice.getTitle());
        assertThat(noticeResponse.url()).isEqualTo(notice.getUrl());
        assertThat(noticeResponse.createdAt()).isEqualTo(notice.getCreatedAt());
        assertThat(noticeResponse.contents()).isEqualTo("새로운 공지가 게시되었습니다.");
    }
}
