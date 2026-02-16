package until.the.eternity.dcs.domain.user.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.user.dto.request.UserSummaryCreateRequest;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryPersistResponse;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSummaryConverterTest 단위 테스트")
public class UserSummaryConverterTest {
    private UserSummaryConverter converter;
    private UserSummary user;

    @BeforeEach
    public void setUp() {
        converter = new UserSummaryConverter();
        user = UserSummary.builder().id(1L).nickname("test").level(1).grade(UserGrade.USER).build();
    }

    @Test
    @DisplayName("createUserSummaryToUserSummary 성공")
    void createUserSummaryToUserSummary_Success() {
        // given
        UserSummaryCreateRequest createRequest =
                new UserSummaryCreateRequest(1L, "test", 1, "user");
        // when
        UserSummary result = converter.createUserSummaryToUserSummary(createRequest);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("test");
        assertThat(result.getLevel()).isEqualTo(1);
        assertThat(result.getGrade()).isEqualTo(UserGrade.USER);
    }

    @Test
    @DisplayName("userSummaryToUserSummaryPersistResponse 성공")
    void userSummaryToUserSummaryPersistResponse_Success() {
        // when
        UserSummaryPersistResponse result = converter.userSummaryToUserSummaryPersistResponse(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("userSummaryToUserSummaryDetailResponse 성공")
    void userSummaryToUserSummaryDetailResponse_Success() {
        // when
        UserSummaryDetailResponse result = converter.userSummaryToUserSummaryDetailResponse(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("test");
        assertThat(result.grade()).isEqualTo("user");
    }
}
