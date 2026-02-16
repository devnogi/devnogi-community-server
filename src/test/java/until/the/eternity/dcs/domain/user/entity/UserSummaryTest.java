package until.the.eternity.dcs.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSummary update 테스트")
public class UserSummaryTest {

    @Test
    @DisplayName("update 성공")
    void updateUserSummary() {
        // given
        UserSummary userSummary =
                UserSummary.builder()
                        .id(1L)
                        .nickname("test")
                        .level(1)
                        .grade(UserGrade.USER)
                        .build();

        // when
        userSummary.update("newNickname", 20, UserGrade.ADMIN);

        // then
        assertThat(userSummary.getNickname()).isEqualTo("newNickname");
        assertThat(userSummary.getLevel()).isEqualTo(20);
        assertThat(userSummary.getGrade()).isEqualTo(UserGrade.ADMIN);
    }
}
