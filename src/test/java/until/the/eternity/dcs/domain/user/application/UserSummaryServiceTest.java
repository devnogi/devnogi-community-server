package until.the.eternity.dcs.domain.user.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.user.dto.request.UserSummaryCreateRequest;
import until.the.eternity.dcs.domain.user.dto.request.UserSummaryUpdateRequest;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryPersistResponse;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;
import until.the.eternity.dcs.domain.user.exception.UserAlreadyExistsException;
import until.the.eternity.dcs.domain.user.exception.UserGradeNotFoundException;
import until.the.eternity.dcs.domain.user.exception.UserNotFoundException;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSummaryService 단위 테스트")
class UserSummaryServiceTest {

    @Mock private UserSummaryRepository userSummaryRepository;

    @Mock private UserSummaryConverter userSummaryConverter;

    @InjectMocks private UserSummaryService userSummaryService;

    private UserSummary userSummary;
    private UserSummaryCreateRequest createRequest;
    private UserSummaryUpdateRequest updateRequest;
    private UserSummaryPersistResponse persistResponse;
    private UserSummaryDetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        userSummary =
                UserSummary.builder()
                        .id(1L)
                        .nickname("testUser")
                        .level(10)
                        .grade(UserGrade.USER)
                        .build();

        createRequest = new UserSummaryCreateRequest(1L, "testUser", 10, "user");
        updateRequest = new UserSummaryUpdateRequest(1L, "updatedUser", 20, "admin");
        persistResponse = new UserSummaryPersistResponse(1L);
        detailResponse = new UserSummaryDetailResponse(1L, "testUser", "user");
    }

    @Nested
    @DisplayName("UserSummary 생성")
    class CreateUserSummary {

        @Test
        @DisplayName("성공: 새로운 UserSummary를 생성한다")
        void createUserSummary_Success() {
            // given
            given(userSummaryRepository.existsById(1L)).willReturn(false);
            given(userSummaryConverter.createUserSummaryToUserSummary(createRequest))
                    .willReturn(userSummary);
            given(userSummaryRepository.save(userSummary)).willReturn(userSummary);
            given(userSummaryConverter.userSummaryToUserSummaryPersistResponse(userSummary))
                    .willReturn(persistResponse);

            // when
            UserSummaryPersistResponse result = userSummaryService.createUserSummary(createRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1L);

            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryConverter).createUserSummaryToUserSummary(createRequest);
            verify(userSummaryRepository).save(userSummary);
            verify(userSummaryConverter).userSummaryToUserSummaryPersistResponse(userSummary);
        }

        @Test
        @DisplayName("실패: 이미 존재하는 사용자인 경우 UserAlreadyExistsException 발생")
        void createUserSummary_ThrowsUserAlreadyExistsException_WhenUserExists() {
            // given
            given(userSummaryRepository.existsById(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userSummaryService.createUserSummary(createRequest))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryConverter, never()).createUserSummaryToUserSummary(any());
            verify(userSummaryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("UserSummary 조회")
    class FindUserSummary {

        @Test
        @DisplayName("성공: 존재하는 UserSummary을 조회한다")
        void findUserSummary_Success() {
            // given
            given(userSummaryRepository.existsById(1L)).willReturn(true);
            given(userSummaryRepository.findById(1L)).willReturn(Optional.of(userSummary));
            given(userSummaryConverter.userSummaryToUserSummaryDetailResponse(userSummary))
                    .willReturn(detailResponse);

            // when
            UserSummaryDetailResponse result = userSummaryService.findUserSummary(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.nickname()).isEqualTo("testUser");

            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryRepository).findById(1L);
            verify(userSummaryConverter).userSummaryToUserSummaryDetailResponse(userSummary);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자인 경우 UserNotFoundException 발생")
        void findUserSummary_ThrowsUserNotFoundException_WhenUserNotExists() {
            // given
            given(userSummaryRepository.existsById(1L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userSummaryService.findUserSummary(1L))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("UserSummary 수정")
    class UpdateUserSummary {

        @Test
        @DisplayName("성공: 존재하는 UserSummary를 수정한다")
        void updateUserSummary_Success() {
            // given
            given(userSummaryRepository.existsById(1L)).willReturn(true);
            given(userSummaryRepository.findById(1L)).willReturn(Optional.of(userSummary));
            given(userSummaryRepository.save(userSummary)).willReturn(userSummary);
            given(userSummaryConverter.userSummaryToUserSummaryPersistResponse(userSummary))
                    .willReturn(persistResponse);

            // when
            UserSummaryPersistResponse result = userSummaryService.updateUserSummary(updateRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1L);

            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryRepository).findById(1L);
            assertThat(userSummary.getNickname()).isEqualTo("updatedUser");
            assertThat(userSummary.getGrade()).isEqualTo(UserGrade.ADMIN);
            assertThat(userSummary.getLevel()).isEqualTo(20);
            verify(userSummaryRepository).save(userSummary);
            verify(userSummaryConverter).userSummaryToUserSummaryPersistResponse(userSummary);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자인 경우 UserNotFoundException 발생")
        void updateUserSummary_ThrowsUserNotFoundException_WhenUserNotExists() {
            // given
            given(userSummaryRepository.existsById(1L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userSummaryService.updateUserSummary(updateRequest))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryRepository, never()).findById(any());
            verify(userSummaryRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 잘못된 등급 코드인 경우 UserGradeNotFoundException 발생")
        void updateUserSummary_ThrowsUserGradeNotFoundException_WhenInvalidGradeCode() {
            // given
            UserSummaryUpdateRequest invalidGradeRequest =
                    new UserSummaryUpdateRequest(1L, "updatedUser", 20, "INVALID_GRADE");
            given(userSummaryRepository.existsById(1L)).willReturn(true);
            given(userSummaryRepository.findById(1L)).willReturn(Optional.of(userSummary));

            // when & then
            assertThatThrownBy(() -> userSummaryService.updateUserSummary(invalidGradeRequest))
                    .isInstanceOf(UserGradeNotFoundException.class);

            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryRepository).findById(1L);
            verify(userSummaryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("UserSummary 삭제")
    class DeleteUserSummary {

        @Test
        @DisplayName("성공: 존재하는 UserSummary를 삭제한다")
        void deleteUserSummary_Success() {
            // given
            given(userSummaryRepository.existsById(1L)).willReturn(true);

            // when
            userSummaryService.deleteUserSummary(1L);

            // then
            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryRepository).deleteById(1L);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자인 경우 UserNotFoundException 발생")
        void deleteUserSummary_ThrowsUserNotFoundException_WhenUserNotExists() {
            // given
            given(userSummaryRepository.existsById(1L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userSummaryService.deleteUserSummary(1L))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userSummaryRepository).existsById(1L);
            verify(userSummaryRepository, never()).deleteById(any());
        }
    }
}
