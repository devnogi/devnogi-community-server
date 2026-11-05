package until.the.eternity.dcs.domain.user.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;

class UserSummaryDltListenerTest {
    private final UserSummaryDltInboxService inbox = mock(UserSummaryDltInboxService.class);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);
    private final UserSummaryDltListener listener = new UserSummaryDltListener(inbox);

    @Test
    void persistsUsingOriginalSourceIdentityBeforeAck() {
        var record = sourceRecord();
        listener.consume(record, acknowledgment);
        var order = inOrder(inbox, acknowledgment);
        order.verify(inbox).store("USER_INFO_UPDATE_EVENT", 2, 73L, record.value());
        order.verify(acknowledgment).acknowledge();
    }

    @Test
    void storageFailureLeavesOffsetUnacknowledged() {
        var record = sourceRecord();
        doThrow(new IllegalStateException("database unavailable"))
                .when(inbox)
                .store("USER_INFO_UPDATE_EVENT", 2, 73L, record.value());
        assertThatThrownBy(() -> listener.consume(record, acknowledgment))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(acknowledgment);
    }

    @Test
    void missingSourceHeadersLeaveOffsetUnacknowledged() {
        var record =
                new ConsumerRecord<String, byte[]>(
                        "USER_INFO_UPDATE_EVENT.DLT", 2, 99L, null, new byte[0]);
        assertThatThrownBy(() -> listener.consume(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(inbox, acknowledgment);
    }

    private ConsumerRecord<String, byte[]> sourceRecord() {
        var record =
                new ConsumerRecord<String, byte[]>(
                        "USER_INFO_UPDATE_EVENT.DLT",
                        2,
                        99L,
                        null,
                        "{}".getBytes(StandardCharsets.UTF_8));
        record.headers()
                .add(
                        KafkaHeaders.DLT_ORIGINAL_TOPIC,
                        "USER_INFO_UPDATE_EVENT".getBytes(StandardCharsets.UTF_8));
        record.headers()
                .add(
                        KafkaHeaders.DLT_ORIGINAL_PARTITION,
                        ByteBuffer.allocate(Integer.BYTES).putInt(2).array());
        record.headers()
                .add(
                        KafkaHeaders.DLT_ORIGINAL_OFFSET,
                        ByteBuffer.allocate(Long.BYTES).putLong(73L).array());
        return record;
    }
}
