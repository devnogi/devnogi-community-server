package until.the.eternity.dcs.domain.announcement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.announcement.entity.AnnouncementRepository;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementDuplicateException;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostModifyForbiddenException;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

class AnnouncementServiceTest {
    AnnouncementService announcementService;
    AnnouncementRepository announcementRepository;
    PostRepository postRepository;
    PostMetaRepository postMetaRepository;
    UserService userService;
    Long id = 1L;
    Post post;
    Announcement announcement;

    @BeforeEach
    void init() {
        announcementRepository = Mockito.mock(AnnouncementRepository.class);
        postRepository = Mockito.mock(PostRepository.class);
        postMetaRepository = Mockito.mock(PostMetaRepository.class);
        userService = Mockito.mock(UserService.class);

        AnnouncementConverter converter = new AnnouncementConverter();

        announcementService =
                new AnnouncementService(
                        announcementRepository,
                        converter,
                        postRepository,
                        postMetaRepository,
                        userService);

        announcement = Announcement.builder().id(id).userId(id).isGlobal(true).build();
        post = Post.builder().id(id).build();
    }

    @Test
    @DisplayName("create는 새로운 Announcement를 생성, 저장한다.")
    void create_Success() {
        // given
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id))
                .thenReturn(Optional.of(post));
        when(announcementRepository.save(Mockito.any(Announcement.class))).thenReturn(announcement);
        AnnouncementCreateRequest request = new AnnouncementCreateRequest(true);

        // when
        AnnouncementPersistResponse response = announcementService.create(id, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("create는 이미 Announcement로 등록되어 있는 경우 AnnouncementDuplicateException을 리턴한다.")
    void create_DuplicateException() {
        // given
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id))
                .thenReturn(Optional.of(post));
        when(announcementRepository.save(Mockito.any(Announcement.class))).thenReturn(announcement);
        when(announcementRepository.existsByPostId(post.getId())).thenReturn(true);
        AnnouncementCreateRequest request = new AnnouncementCreateRequest(true);

        // when
        // then
        assertThatThrownBy(() -> announcementService.create(id, request))
                .isInstanceOf(AnnouncementDuplicateException.class);
    }

    @Test
    @DisplayName("delete는 인가되지 않은 User의 Announcement 수정에 대해 PostModifyForbiddenException를 리턴한다.")
    void delete_ForbiddenException() {
        // given
        when(announcementRepository.findById(id)).thenReturn(Optional.of(announcement));
        when(userService.getCurrentUser()).thenReturn(UserSummary.builder().id(999L).build());

        // when
        // then
        assertThatThrownBy(() -> announcementService.delete(id))
                .isInstanceOf(PostModifyForbiddenException.class);
    }

    @Test
    @DisplayName("toggleGlobal은 Announcement의 isGlobal을 토글한다.")
    void toggleGlobal_Success() {
        // given
        when(announcementRepository.findById(id)).thenReturn(Optional.of(announcement));

        // when
        announcementService.toggleGlobal(id);

        // then
        assertThat(announcement.getIsGlobal()).isFalse();

        // when
        announcementService.toggleGlobal(id);

        // then
        assertThat(announcement.getIsGlobal()).isTrue();
    }
}
