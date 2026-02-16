package until.the.eternity.dcs.domain.announcement.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPageResponseItem;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementBoardNotFoundException;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementDuplicateException;
import until.the.eternity.dcs.domain.announcement.infrastructure.AnnouncementRepository;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.infrastructure.BoardRepository;
import until.the.eternity.dcs.domain.post.application.PostMetaService;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnnouncementServiceTest {
    AnnouncementService announcementService;
    AnnouncementRepository announcementRepository;
    PostRepository postRepository;
    PostMetaRepository postMetaRepository;
    PostMetaService postMetaService;
    BoardRepository boardRepository;
    Long id = 1L;
    Post post;
    Announcement announcement;
    Long userId = 1L;
    PostMeta postMeta;
    Integer commentCount = 1;
    PostMetaResponse postMetaResponse;

    @BeforeEach
    void init() {
        announcementRepository = mock(AnnouncementRepository.class);
        postRepository = mock(PostRepository.class);
        postMetaRepository = mock(PostMetaRepository.class);
        boardRepository = mock(BoardRepository.class);
        RedisSender redisSender = mock(RedisSender.class);
        AnnouncementPermissionEvaluator announcementPermissionEvaluator =
                mock(AnnouncementPermissionEvaluator.class);
        postMetaService = mock(PostMetaService.class);
        AnnouncementConverter converter = new AnnouncementConverter();

        announcementService =
                new AnnouncementService(
                        announcementRepository,
                        converter,
                        postRepository,
                        postMetaRepository,
                        postMetaService,
                        redisSender,
                        announcementPermissionEvaluator,
                        boardRepository);

        announcement = Announcement.builder().id(id).userId(userId).isGlobal(true).build();
        post =
                Post.builder()
                        .id(id)
                        .board(Board.builder().announcements(new ArrayList<>()).build())
                        .build();
        postMeta = PostMeta.create(id, commentCount);
        postMetaResponse = PostMetaResponse.from(postMeta);
    }

    @Test
    @DisplayName("create는 Announcement를 생성하고 저장한다.")
    void create_Success() {
        // given
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id))
                .thenReturn(Optional.of(post));
        when(announcementRepository.save(Mockito.any(Announcement.class))).thenReturn(announcement);
        when(postMetaService.getPostMetaInfo(id)).thenReturn(postMetaResponse);
        AnnouncementCreateRequest request = new AnnouncementCreateRequest(true);

        // when
        AnnouncementPersistResponse response = announcementService.create(id, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(post.getBoard().getAnnouncements().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("create는 이미 Announcement로 등록된 경우 AnnouncementDuplicateException을 발생시킨다.")
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
    @DisplayName("toggleGlobal은 Announcement의 isGlobal 값을 토글한다.")
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

    @Test
    @DisplayName("getAnnouncements는 boardId로 해당 게시판 공지와 전체 공지를 함께 조회한다.")
    void getAnnouncements_WithBoardIdSuccess() {
        // given
        when(boardRepository.findByIdAndIsDeletedIsFalse(id))
                .thenReturn(Optional.of(Board.builder().id(id).build()));
        when(announcementRepository.findActiveByBoardIdOrGlobal(id)).thenReturn(List.of(announcement));

        // when
        List<AnnouncementPageResponseItem> response = announcementService.getAnnouncements(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0).postId()).isEqualTo(announcement.getPostId());
        assertThat(response.get(0).title()).isEqualTo(announcement.getTitle());
        assertThat(response.get(0).isGlobal()).isEqualTo(announcement.getIsGlobal());
    }

    @Test
    @DisplayName("getAnnouncements는 boardId가 null이면 전체 공지(isGlobal=true, isDraft=false)만 조회한다.")
    void getAnnouncements_WithoutBoardIdSuccess() {
        // given
        when(announcementRepository.findByIsDraftFalseAndIsGlobalTrue())
                .thenReturn(List.of(announcement));

        // when
        List<AnnouncementPageResponseItem> response = announcementService.getAnnouncements(null);

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).isGlobal()).isTrue();
        assertThat(response.get(0).postId()).isEqualTo(announcement.getPostId());
        assertThat(response.get(0).title()).isEqualTo(announcement.getTitle());
    }

    @Test
    @DisplayName("boardId가 존재하지 않으면 AnnouncementBoardNotFoundException을 던진다.")
    void getAnnouncements_BoardNotFound() {
        // given
        when(boardRepository.findByIdAndIsDeletedIsFalse(id)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> announcementService.getAnnouncements(id))
                .isInstanceOf(AnnouncementBoardNotFoundException.class);
    }
}
