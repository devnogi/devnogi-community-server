package until.the.eternity.dcs.common.notification.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

class NotificationJobTest {
    @Test
    void existingInternalNotificationRoundTripsWithEmptyChannel() {
        NotificationJob job = NotificationJob.of(1L, NoticeType.POST_LIKE, 2L);

        assertThat(NotificationJob.fromMap(job.toMap())).isEqualTo(job);
        assertThat(job.channel()).isEmpty();
    }

    @Test
    void broadcastUserIdRemainsValid() {
        NotificationJob job = NotificationJob.of(0L, NoticeType.POST_LIKE, 2L);

        assertThat(NotificationJob.fromMap(job.toMap()).userId()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"notificationId", "userId", "channel", "noticeType", "url"})
    void missingRequiredFieldIsRejected(String field) {
        Map<String, String> values = validMap();
        values.remove(field);

        assertThatThrownBy(() -> NotificationJob.fromMap(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(field);
    }

    @ParameterizedTest
    @ValueSource(strings = {"notificationId", "userId", "noticeType", "url"})
    void blankRequiredFieldIsRejected(String field) {
        Map<String, String> values = validMap();
        values.put(field, " \t");

        assertThatThrownBy(() -> NotificationJob.fromMap(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(field);
    }

    @ParameterizedTest
    @ValueSource(strings = {"private-user-value", "9223372036854775808", "1.5"})
    void invalidNumberIsRejectedWithoutEchoingInput(String input) {
        Map<String, String> values = validMap();
        values.put("userId", input);

        assertThatThrownBy(() -> NotificationJob.fromMap(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId must be a valid integer");
    }

    @Test
    void unknownNoticeTypeIsRejectedWithoutEchoingInput() {
        Map<String, String> values = validMap();
        values.put("noticeType", "private-unknown-value");

        assertThatThrownBy(() -> NotificationJob.fromMap(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("noticeType is unknown");
    }

    private Map<String, String> validMap() {
        return new HashMap<>(NotificationJob.of(1L, NoticeType.POST_LIKE, 2L).toMap());
    }
}
