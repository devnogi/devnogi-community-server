package until.the.eternity.dcs.domain.user.application;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSummaryDltListener {
    private final UserSummaryDltInboxService inboxService;

    @KafkaListener(
            topics = "USER_INFO_UPDATE_EVENT.DLT",
            groupId = "devnogi-community-user-summary-dlt",
            containerFactory = "userSummaryDltKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, byte[]> record, Acknowledgment acknowledgment) {
        // Use the failed source record's identity, not the DLT copy's offset.
        String sourceTopic =
                new String(
                        requiredHeader(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                        StandardCharsets.UTF_8);
        int sourcePartition =
                ByteBuffer.wrap(requiredHeader(record, KafkaHeaders.DLT_ORIGINAL_PARTITION))
                        .getInt();
        long sourceOffset =
                ByteBuffer.wrap(requiredHeader(record, KafkaHeaders.DLT_ORIGINAL_OFFSET)).getLong();
        inboxService.store(sourceTopic, sourcePartition, sourceOffset, record.value());
        acknowledgment.acknowledge();
    }

    private byte[] requiredHeader(ConsumerRecord<String, byte[]> record, String name) {
        var header = record.headers().lastHeader(name);
        if (header == null) {
            throw new IllegalArgumentException("DLT record is missing source header: " + name);
        }
        return header.value();
    }
}
