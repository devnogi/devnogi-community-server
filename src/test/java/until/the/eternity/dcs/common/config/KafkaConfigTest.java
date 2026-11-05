package until.the.eternity.dcs.common.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaConfigTest {
    @Test
    void failedDltSendThrowsInsteadOfCompletingRecovery() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<Object, byte[]> template = mock(KafkaTemplate.class);
        when(template.partitionsFor("USER_INFO_UPDATE_EVENT.DLT"))
                .thenReturn(
                        List.of(
                                new PartitionInfo(
                                        "USER_INFO_UPDATE_EVENT.DLT", 0, null, null, null)));
        when(template.send(any(ProducerRecord.class)))
                .thenReturn(
                        CompletableFuture.<SendResult<Object, byte[]>>failedFuture(
                                new IllegalStateException("synthetic DLT send failure")));

        var recoverer = new KafkaConfig().userSummaryDeadLetterPublishingRecoverer(template);
        var source =
                new ConsumerRecord<Object, byte[]>(
                        "USER_INFO_UPDATE_EVENT", 0, 7L, 42L, new byte[] {1, 2, 3});
        assertThatThrownBy(
                        () ->
                                recoverer.accept(
                                        source, new IllegalArgumentException("synthetic failure")))
                .hasRootCauseMessage("synthetic DLT send failure");
        verify(template).send(any(ProducerRecord.class));
    }
}
