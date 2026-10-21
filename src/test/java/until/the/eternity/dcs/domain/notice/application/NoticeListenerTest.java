package until.the.eternity.dcs.domain.notice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

class NoticeListenerTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final NoticeService noticeService = mock(NoticeService.class);
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final StreamOperations<String, String, String> streams = mock(StreamOperations.class);
    private NoticeListener listener;

    @BeforeEach
    void setUp() {
        listener = new NoticeListener(redis, noticeService);
        listener.group = "test-group";
        when(redis.opsForValue()).thenReturn(values);
        when(redis.<String, String>opsForStream()).thenReturn(streams);
    }

    @Test
    void exactLegacyRecordOnlyAcknowledges() {
        MapRecord<String, String, String> record = record(Map.of("mk", "1"));

        listener.handle(record);

        verify(streams).acknowledge("test-stream", "test-group", record.getId());
        verifyNoInteractions(values, noticeService);
    }

    @Test
    void legacyMarkerWithExtraFieldsIsMalformedAndHasNoSideEffects() {
        listener.handle(record(Map.of("mk", "1", "unexpected", "private-value")));

        verifyNoInteractions(values, noticeService, streams);
    }

    @Test
    void malformedRecordDoesNotEscapeOrPreventNextValidDelivery() {
        when(values.setIfAbsent(anyString(), anyString(), eq(Duration.ofDays(7)))).thenReturn(true);

        assertThatCode(() -> listener.handle(record(Map.of("userId", "private-value"))))
                .doesNotThrowAnyException();
        verifyNoInteractions(values, noticeService, streams);

        MapRecord<String, String, String> valid = record(job().toMap());
        listener.handle(valid);

        verify(noticeService).createNotice(any(NoticeSendRequest.class));
        verify(streams).acknowledge("test-stream", "test-group", valid.getId());
    }

    @Test
    void successfulBusinessCallFinishesBeforeAcknowledgementAndKeepsClaim() {
        when(values.setIfAbsent(anyString(), anyString(), eq(Duration.ofDays(7)))).thenReturn(true);
        MapRecord<String, String, String> record = record(job().toMap());

        listener.handle(record);

        InOrder ordered = inOrder(noticeService, streams);
        ordered.verify(noticeService).createNotice(any(NoticeSendRequest.class));
        ordered.verify(streams).acknowledge("test-stream", "test-group", record.getId());
        verify(redis, never()).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }

    @Test
    void failedBusinessCallReleasesOnlyTheAcquiredTokenAndDoesNotAcknowledge() {
        when(values.setIfAbsent(anyString(), anyString(), eq(Duration.ofDays(7)))).thenReturn(true);
        when(noticeService.createNotice(any())).thenThrow(new IllegalStateException("DB failure"));
        NotificationJob job = job();

        listener.handle(record(job.toMap()));

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(values)
                .setIfAbsent(
                        eq("notif:processed:" + job.notificationId()),
                        token.capture(),
                        eq(Duration.ofDays(7)));
        verify(redis)
                .execute(
                        any(RedisScript.class),
                        eq(List.of("notif:processed:" + job.notificationId())),
                        eq(token.getValue()));
        assertThat(token.getValue()).isNotBlank().isNotEqualTo("1");
        verifyNoInteractions(streams);
    }

    @Test
    void existingClaimAcknowledgesWithoutRunningBusinessLogic() {
        when(values.setIfAbsent(anyString(), anyString(), eq(Duration.ofDays(7))))
                .thenReturn(false);
        MapRecord<String, String, String> record = record(job().toMap());

        listener.handle(record);

        verifyNoInteractions(noticeService);
        verify(streams).acknowledge("test-stream", "test-group", record.getId());
        verify(redis, never()).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }

    @Test
    void unknownClaimResultIsNotTreatedAsAcquiredOrAcknowledged() {
        when(values.setIfAbsent(anyString(), anyString(), eq(Duration.ofDays(7)))).thenReturn(null);
        listener.handle(record(job().toMap()));

        verifyNoInteractions(noticeService, streams);
    }

    @Test
    void acknowledgementFailureAfterBusinessSuccessDoesNotReleaseClaim() {
        when(values.setIfAbsent(anyString(), anyString(), eq(Duration.ofDays(7)))).thenReturn(true);
        when(streams.acknowledge(anyString(), anyString(), any(RecordId.class)))
                .thenThrow(new IllegalStateException("Redis failure"));

        assertThatCode(() -> listener.handle(record(job().toMap()))).doesNotThrowAnyException();

        verify(noticeService).createNotice(any());
        verify(redis, never()).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }

    @Test
    void claimFailureDoesNotEscapeOrAcknowledge() {
        when(values.setIfAbsent(anyString(), anyString(), eq(Duration.ofDays(7))))
                .thenThrow(new IllegalStateException("Redis failure"));

        assertThatCode(() -> listener.handle(record(job().toMap()))).doesNotThrowAnyException();

        verifyNoInteractions(noticeService, streams);
    }

    private NotificationJob job() {
        return NotificationJob.of(1L, NoticeType.POST_LIKE, 2L);
    }

    private MapRecord<String, String, String> record(Map<String, String> values) {
        return StreamRecords.mapBacked(values)
                .withStreamKey("test-stream")
                .withId(RecordId.of("1-0"));
    }
}
