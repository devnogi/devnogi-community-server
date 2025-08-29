package until.the.eternity.dcs.domain.notice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_LIKE;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.dto.response.NoticePersistResponse;
import until.the.eternity.dcs.domain.notice.entity.Notice;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

class NoticeConverterTest {
    NoticeConverter noticeConverter = new NoticeConverter();

    Long id = 1L;
    Long userId = 1L;
    NoticeType noticeType = POST_LIKE;
    String url = "api/posts/1";

    @Test
    @DisplayName("fromSendRequest는 NoticeSendRequest를 Notice로 변환한다.")
    void fromSendRequest_Success() {
        // given
        NoticeSendRequest request = new NoticeSendRequest(id, noticeType, url, userId);

        // when
        Notice notice = noticeConverter.fromSendRequest(request);

        // then
        assertThat(notice).isNotNull();
        assertThat(notice.getNoticeType()).isEqualTo(noticeType);
        assertThat(notice.getUrl()).isEqualTo(url);
    }

    @Test
    @DisplayName("toNoticeCommonResponse는 Notice를 NoticeCommonResponse로 변환한다.")
    void toNoticeCommonResponse_Success() {
        // given
        Notice notice =
                Notice.builder()
                        .id(id)
                        .title(noticeType.getDescription())
                        .noticeType(noticeType)
                        .createdAt(LocalDateTime.now())
                        .url(url)
                        .build();
        // todo
        // when
        //        NoticeCommonResponse response = noticeConverter.toNoticeCommonResponse(notice);
        //
        //        // then
        //        assertThat(response).isNotNull();
        //        assertThat(response.id()).isEqualTo(id);
        //        assertThat(response.userId()).isEqualTo(id);
        //        assertThat(response.title()).isEqualTo(noticeType.getDescription());
        //        assertThat(response.contents()).isEqualTo("회원님의 게시글에 좋아요가 달렸습니다.");
        //        assertThat(response.createdAt()).isEqualTo(notice.getCreatedAt());
        //        assertThat(response.url()).isEqualTo(url);
        //        assertThat(response.isRead()).isFalse();
    }

    @Test
    @DisplayName("toNoticePersistResponse Notice를 NoticePersistResponse로 변환한다.")
    void toNoticePersistResponse_Success() {
        // given
        Notice notice = Notice.builder().id(id).build();

        // when
        NoticePersistResponse response = noticeConverter.toNoticePersistResponse(notice);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
    }
}
