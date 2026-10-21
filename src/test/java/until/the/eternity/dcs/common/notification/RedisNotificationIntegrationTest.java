package until.the.eternity.dcs.common.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.config.JpaConfig;
import until.the.eternity.dcs.common.config.QueryDslConfig;
import until.the.eternity.dcs.common.config.RedisStreamConfig;
import until.the.eternity.dcs.common.config.RedisStreamListenerConfig;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.notice.application.NoticeConverter;
import until.the.eternity.dcs.domain.notice.application.NoticeListener;
import until.the.eternity.dcs.domain.notice.application.NoticeService;
import until.the.eternity.dcs.domain.notice.application.NoticeUserConverter;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;
import until.the.eternity.dcs.domain.notice.infrastructure.NoticeRepository;
import until.the.eternity.dcs.domain.notice.infrastructure.NoticeUserRepository;

@Tag("redis-integration")
@DataJpaTest(showSql = false, properties = "spring.jpa.properties.hibernate.show_sql=false")
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    NoticeService.class,
    NoticeConverter.class,
    NoticeUserConverter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ExtendWith(OutputCaptureExtension.class)
class RedisNotificationIntegrationTest {
    @Autowired private NoticeService noticeService;
    @Autowired private NoticeRepository notices;
    @Autowired private NoticeUserRepository recipients;
    @Autowired private JdbcTemplate jdbc;

    private final Set<String> ownedKeys = new HashSet<>();
    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redis;
    private StreamOperations<String, String, String> streams;
    private RedisStreamConfig initializer;
    private RedisSender sender;
    private NoticeListener listener;
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private ThreadPoolTaskExecutor executor;
    private String prefix;
    private String stream;
    private String group;

    @BeforeEach
    void connectToExplicitIsolatedRedis() {
        String host = System.getenv("TEST_REDIS_HOST");
        String port = System.getenv("TEST_REDIS_PORT");
        assertThat(host).as("TEST_REDIS_HOST is required").isNotBlank();
        assertThat(port).as("TEST_REDIS_PORT is required").isNotBlank();
        connectionFactory =
                new LettuceConnectionFactory(
                        new RedisStandaloneConfiguration(host, Integer.parseInt(port)),
                        LettuceClientConfiguration.builder()
                                .commandTimeout(Duration.ofSeconds(3))
                                .shutdownTimeout(Duration.ZERO)
                                .build());
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();
        redis = new StringRedisTemplate(connectionFactory);
        streams = redis.opsForStream();
        try (var connection = connectionFactory.getConnection()) {
            assertThat(connection.ping()).isEqualTo("PONG");
            String version =
                    connection.serverCommands().info("server").getProperty("redis_version");
            assertThat(version).as("The approved integration target is Redis 7").startsWith("7.");
            System.out.println("Redis integration version=" + version);
        }
        prefix = "community-redis-test:" + UUID.randomUUID();
        stream = prefix + ":stream";
        group = prefix + ":group";
        ownedKeys.add(stream);
        initializer = initializer(group);
        sender = new RedisSender(redis);
        ReflectionTestUtils.setField(sender, "stream", stream);
        ReflectionTestUtils.setField(sender, "maxLen", 1000L);
        listener = listener(noticeService);
        recipients.deleteAll();
        notices.deleteAll();
    }

    @AfterEach
    void cleanOnlyThisTestsResources() {
        if (container != null) {
            container.stop();
        }
        if (executor != null) {
            executor.shutdown();
        }
        try {
            if (redis != null && !ownedKeys.isEmpty()) {
                // The nested Spring lifecycle stops its shared factory when the context closes.
                if (!connectionFactory.isRunning()) {
                    connectionFactory.start();
                }
                redis.delete(ownedKeys);
            }
        } finally {
            if (connectionFactory != null) {
                connectionFactory.destroy();
            }
        }
    }

    @Test
    void emptyInitializationAndRepeatedInitializationPreserveRecordsOffsetsAndPending() {
        initializer.initStreamAndGroup();
        assertThat(streams.size(stream)).isZero();
        assertThat(streams.groups(stream)).hasSize(1);

        RecordId first = enqueue(job(41L));
        RecordId second = enqueue(job(42L));
        MapRecord<String, String, String> firstDelivery = readNew();
        assertThat(firstDelivery.getId()).isEqualTo(first);
        var beforeRecords = streams.range(stream, Range.unbounded());
        var beforeGroup = streams.groups(stream).get(0);
        Set<RecordId> beforePending = pendingIds();

        initializer.initStreamAndGroup();
        var afterGroup = streams.groups(stream).get(0);
        assertThat(streams.range(stream, Range.unbounded())).isEqualTo(beforeRecords);
        assertThat(afterGroup.lastDeliveredId()).isEqualTo(beforeGroup.lastDeliveredId());
        assertThat(afterGroup.pendingCount()).isEqualTo(beforeGroup.pendingCount());
        assertThat(pendingIds()).isEqualTo(beforePending).containsExactly(first);
        assertThat(readNew().getId()).isEqualTo(second);

        String backlogGroup = group + ":backlog";
        initializer(backlogGroup).initStreamAndGroup();
        var backlog =
                streams.read(
                        Consumer.from(backlogGroup, "backlog-consumer"),
                        StreamReadOptions.empty().count(10),
                        StreamOffset.create(stream, ReadOffset.lastConsumed()));
        assertThat(backlog).extracting(MapRecord::getId).containsExactly(first, second);
        System.out.println(
                "preserved group offset="
                        + afterGroup.lastDeliveredId()
                        + " pending="
                        + beforePending
                        + " backlog="
                        + backlog.size());
    }

    @Test
    void initializationDoesNotHideWrongTypeFailure() {
        redis.opsForValue().set(stream, "test-only-wrong-type");
        assertThatThrownBy(initializer::initStreamAndGroup)
                .isInstanceOf(DataAccessException.class)
                .hasStackTraceContaining("WRONGTYPE");
        assertThat(redis.opsForValue().get(stream)).isEqualTo("test-only-wrong-type");
    }

    @Test
    void productionContainerDependsOnSuccessfulInitializationEvenWhenInitializerIsLazy() {
        try (AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext()) {
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    "isolated-redis-test",
                                    Map.of(
                                            "app.notification.stream", stream,
                                            "app.notification.group", group,
                                            "app.notification.pollTimeoutSec", "1")));
            context.getBeanFactory().registerSingleton("redisConnectionFactory", connectionFactory);
            context.getBeanFactory().registerSingleton("stringRedisTemplate", redis);
            context.getBeanFactory().registerSingleton("noticeListener", listener);
            context.register(RedisStreamListenerConfig.class);
            context.registerBean(
                    "redisStreamConfig",
                    RedisStreamConfig.class,
                    () -> initializer(group),
                    definition -> definition.setLazyInit(true));
            context.refresh();

            assertThat(context.getBean(StreamMessageListenerContainer.class).isRunning()).isTrue();
            assertThat(streams.size(stream)).isZero();
            assertThat(streams.groups(stream)).hasSize(1);
            NotificationJob job = job(46L);
            RecordId id = enqueue(job);
            await().atMost(Duration.ofSeconds(15))
                    .untilAsserted(
                            () -> {
                                assertRows(1);
                                assertThat(pendingIds()).isEmpty();
                            });
            System.out.println("Spring dependency startup processed=" + id);
        }
    }

    @Test
    void realContainerContinuesAfterLegacyAndMalformedRecords(CapturedOutput output) {
        initializer.initStreamAndGroup();
        RecordId legacy = add(Map.of("mk", "1"));
        List<RecordId> malformed = new ArrayList<>();
        malformed.add(add(Map.of("mk", "1", "unexpected", "payload-must-not-be-logged")));
        List<Map<String, String>> invalidMaps = new ArrayList<>();
        invalidMaps.add(changedJob("notificationId", null));
        invalidMaps.add(changedJob("notificationId", " "));
        invalidMaps.add(changedJob("userId", "not-a-number"));
        invalidMaps.add(changedJob("noticeType", "UNKNOWN"));
        invalidMaps.add(changedJob("channel", null));
        invalidMaps.add(changedJob("url", " "));
        for (Map<String, String> invalid : invalidMaps) {
            malformed.add(add(invalid));
        }
        NotificationJob valid = job(43L);
        RecordId validId = enqueue(valid);
        startProductionContainer();

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(
                        () -> {
                            assertRows(1);
                            assertThat(pendingIds()).containsExactlyInAnyOrderElementsOf(malformed);
                            assertThat(pendingIds()).doesNotContain(legacy, validId);
                        });
        assertThat(notices.findAll().getFirst().getUrl()).isEqualTo(valid.url());
        assertThat(recipients.findAll().getFirst().getUserId()).isEqualTo(valid.userId());
        for (Map<String, String> invalid : invalidMaps) {
            String id = invalid.get("notificationId");
            if (id != null && !id.isBlank()) {
                assertThat(redis.hasKey(claimKey(id))).isFalse();
            }
        }
        assertThat(output.getAll()).doesNotContain("payload-must-not-be-logged");
        System.out.println("legacy=" + legacy + " valid=" + validId + " pending=" + pendingIds());
    }

    @Test
    void databaseFailureRollsBackBothRowsAndManualRedeliveryCanCommitAndSuppressDuplicate() {
        initializer.initStreamAndGroup();
        NotificationJob job = job(44L);
        RecordId failedId = enqueue(job);
        MapRecord<String, String, String> failedDelivery = readNew();
        jdbc.execute(
                "ALTER TABLE notice_user ADD CONSTRAINT test_reject_recipient CHECK (user_id <> 44)");
        try {
            listener.handle(failedDelivery);
            assertRows(0);
            assertThat(redis.hasKey(claimKey(job.notificationId()))).isFalse();
            assertThat(pendingIds()).containsExactly(failedId);
        } finally {
            jdbc.execute("ALTER TABLE notice_user DROP CONSTRAINT test_reject_recipient");
        }

        var redelivery =
                streams.read(
                        Consumer.from(group, "manual-consumer"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(stream, ReadOffset.from("0")));
        assertThat(redelivery).hasSize(1);
        assertThat(redelivery.getFirst().getId()).isEqualTo(failedId);
        listener.handle(redelivery.getFirst());
        assertRows(1);
        assertThat(pendingIds()).isEmpty();
        assertThat(redis.getExpire(claimKey(job.notificationId())))
                .isBetween(Duration.ofDays(6).toSeconds(), Duration.ofDays(7).toSeconds());

        RecordId duplicateId = enqueue(job);
        listener.handle(readNew());
        assertRows(1);
        assertThat(pendingIds()).isEmpty();
        System.out.println(
                "manual recovery record=" + failedId + " duplicate=" + duplicateId + " pending=0");
    }

    @Test
    void failureCannotDeleteAnotherDeliveryClaim() {
        initializer.initStreamAndGroup();
        NotificationJob job = job(45L);
        RecordId id = enqueue(job);
        NoticeService failingService = mock(NoticeService.class);
        doAnswer(
                        invocation -> {
                            redis.opsForValue()
                                    .set(
                                            claimKey(job.notificationId()),
                                            "another-delivery",
                                            Duration.ofDays(7));
                            throw new IllegalStateException("test-only persistence failure");
                        })
                .when(failingService)
                .createNotice(any());
        listener(failingService).handle(readNew());
        assertRows(0);
        assertThat(redis.opsForValue().get(claimKey(job.notificationId())))
                .isEqualTo("another-delivery");
        assertThat(pendingIds()).containsExactly(id);
    }

    private RedisStreamConfig initializer(String selectedGroup) {
        RedisStreamConfig config = new RedisStreamConfig(redis);
        ReflectionTestUtils.setField(config, "stream", stream);
        ReflectionTestUtils.setField(config, "group", selectedGroup);
        return config;
    }

    private NoticeListener listener(NoticeService service) {
        NoticeListener result = new NoticeListener(redis, service);
        ReflectionTestUtils.setField(result, "group", group);
        return result;
    }

    private NotificationJob job(long userId) {
        NotificationJob original = NotificationJob.of(userId, NoticeType.POST_LIKE, 100L);
        NotificationJob unique =
                new NotificationJob(
                        prefix + ":" + original.notificationId(),
                        original.userId(),
                        original.channel(),
                        original.noticeType(),
                        original.url());
        ownedKeys.add(claimKey(unique.notificationId()));
        return unique;
    }

    private Map<String, String> changedJob(String field, String replacement) {
        Map<String, String> map = new HashMap<>(job(43L).toMap());
        map.put("url", "payload-must-not-be-logged");
        if (replacement == null) {
            map.remove(field);
        } else {
            map.put(field, replacement);
        }
        return map;
    }

    private RecordId enqueue(NotificationJob job) {
        return sender.enqueue(job);
    }

    private RecordId add(Map<String, String> value) {
        return streams.add(StreamRecords.mapBacked(value).withStreamKey(stream));
    }

    private MapRecord<String, String, String> readNew() {
        var records =
                streams.read(
                        Consumer.from(group, "manual-consumer"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(stream, ReadOffset.lastConsumed()));
        assertThat(records).hasSize(1);
        return records.getFirst();
    }

    private Set<RecordId> pendingIds() {
        Set<RecordId> ids = new HashSet<>();
        streams.pending(stream, group, Range.unbounded(), 100)
                .forEach(item -> ids.add(item.getId()));
        return ids;
    }

    private void startProductionContainer() {
        RedisStreamListenerConfig config =
                new RedisStreamListenerConfig(connectionFactory, listener);
        ReflectionTestUtils.setField(config, "stream", stream);
        ReflectionTestUtils.setField(config, "group", group);
        ReflectionTestUtils.setField(config, "pollTimeoutSec", 1L);
        executor = (ThreadPoolTaskExecutor) config.notifConsumerExecutor();
        container = config.streamContainer(executor);
    }

    private void assertRows(long count) {
        assertThat(notices.count()).isEqualTo(count);
        assertThat(recipients.count()).isEqualTo(count);
    }

    private String claimKey(String id) {
        return "notif:processed:" + id;
    }
}
