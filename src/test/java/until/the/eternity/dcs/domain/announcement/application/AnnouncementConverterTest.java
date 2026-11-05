package until.the.eternity.dcs.domain.announcement.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.enums.PostContentFormat;

class AnnouncementConverterTest {
    AnnouncementConverter announcementConverter;
    Long id = 1L;
    Long userId = 1L;
    Integer commentCount = 1;

    @BeforeEach
    void init() {
        announcementConverter = new AnnouncementConverter();
    }

    @Test
    @DisplayName(
            "fromCreateRequestAndPost은 AnnouncementCreateRequest, Post, PostMeta로 Announcement를 생성한다.")
    void fromCreateRequestAndPost() {
        // given
        AnnouncementCreateRequest request = new AnnouncementCreateRequest(true);
        Post post =
                Post.builder()
                        .id(id)
                        .board(new Board())
                        .userId(userId)
                        .title("title")
                        .content("content")
                        .isDraft(false)
                        .build();
        PostMeta postMeta = PostMeta.create(id, commentCount);
        PostMetaResponse postMetaResponse = PostMetaResponse.from(postMeta);
        // when
        Announcement announcement =
                announcementConverter.fromCreateRequestAndPost(request, post, postMetaResponse);

        // then
        assertThat(announcement).isNotNull();
        assertThat(announcement.getIsGlobal()).isTrue();
        assertThat(announcement.getIsDraft()).isFalse();
        assertThat(announcement.getPostId()).isEqualTo(post.getId());
    }

    @Test
    void categorylessPlainPostBecomesGlobalAnnouncement() {
        Post post =
                Post.builder()
                        .id(id)
                        .userId(userId)
                        .title("본문 첫 문장")
                        .content("본문 첫 문장")
                        .plainContent("본문 첫 문장")
                        .contentFormat(PostContentFormat.PLAIN_TEXT_V1)
                        .isDraft(false)
                        .build();
        PostMetaResponse postMeta = PostMetaResponse.from(PostMeta.create(id, 0));

        Announcement result =
                announcementConverter.fromCreateRequestAndPost(
                        new AnnouncementCreateRequest(false), post, postMeta);

        assertThat(result.getIsGlobal()).isTrue();
        assertThat(result.getBoard()).isNull();
        assertThat(result.getTitle()).isEqualTo("본문 첫 문장");
    }

    @Test
    @DisplayName("fromEntityToPersistResponse는 Announcement에서 AnnouncementPersistResponse를 생성한다.")
    void fromEntityToPersistResponse() {
        // given
        Announcement announcement = Announcement.builder().id(id).build();

        // when
        AnnouncementPersistResponse response =
                announcementConverter.fromEntityToPersistResponse(announcement);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("fromEntityToToggleResponse는 Announcement에서 fromEntityToToggleResponse를 생성한다.")
    void fromEntityToToggleResponse() {
        // given
        Announcement announcement = Announcement.builder().id(id).isGlobal(true).build();

        // when
        AnnouncementToggleResponse response =
                announcementConverter.fromEntityToToggleResponse(announcement);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.isGlobal()).isTrue();
    }
}
