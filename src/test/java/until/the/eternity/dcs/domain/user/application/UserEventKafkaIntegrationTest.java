package until.the.eternity.dcs.domain.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.backoff.FixedBackOff;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@SpringBootTest(
        classes = UserEventKafkaIntegrationTest.Config.class,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:user-event;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.show_sql=false",
            "spring.flyway.enabled=false",
            "spring.docker.compose.enabled=false",
            "logging.level.org.apache.kafka=WARN",
            "logging.level.kafka=WARN",
            "logging.level.state.change.logger=WARN"
        })
@EmbeddedKafka(partitions = 1, topics = UserEventKafkaIntegrationTest.TOPIC)
@DirtiesContext
@ExtendWith(OutputCaptureExtension.class)
class UserEventKafkaIntegrationTest {
    @Test
    void arcanaDistinguishesLegacyMissingFromExplicitClearAndRejectsStaleRevision()
            throws Exception {
        var mapper = new ObjectMapper();
        var payload =
                mapper.createObjectNode()
                        .put("id", 42L)
                        .put("nickname", "arcana")
                        .putNull("serverName")
                        .put("verified", false)
                        .put("revision", 1L)
                        .put("arcana", "DARK_MAGE");
        sendRawAndAwaitOffset(mapper.writeValueAsBytes(payload));
        assertThat(repository.findById(42L).orElseThrow().getArcana()).isEqualTo("DARK_MAGE");
        payload.put("revision", 2L).remove("arcana");
        sendRawAndAwaitOffset(mapper.writeValueAsBytes(payload));
        assertThat(repository.findById(42L).orElseThrow().getArcana()).isEqualTo("DARK_MAGE");
        payload.put("revision", 1L).putNull("arcana");
        sendRawAndAwaitOffset(mapper.writeValueAsBytes(payload));
        assertThat(repository.findById(42L).orElseThrow().getArcana()).isEqualTo("DARK_MAGE");
        payload.put("revision", 3L).putNull("arcana");
        sendRawAndAwaitOffset(mapper.writeValueAsBytes(payload));
        assertThat(repository.findById(42L).orElseThrow().getArcana()).isNull();
        payload.put("revision", 4L).put("arcana", "UNKNOWN");
        sendRawAndAwaitOffset(mapper.writeValueAsBytes(payload));
        assertThat(rejected.poll(5, TimeUnit.SECONDS)).isNotNull();
        assertThat(repository.findById(42L).orElseThrow().getLastAuthSyncRevision()).isEqualTo(3L);
    }

    static final String TOPIC = "USER_INFO_UPDATE_EVENT";
    private static final String GROUP = "devnogi-community-group";

    @Autowired private UserSummaryRepository repository;
    @Autowired private EmbeddedKafkaBroker broker;
    @Autowired private KafkaListenerEndpointRegistry registry;
    @Autowired private BlockingQueue<Exception> rejected;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserSummaryDltInboxService inbox;

    @BeforeEach
    void prepare() {
        registry.getListenerContainers()
                .forEach(container -> ContainerTestUtils.waitForAssignment(container, 1));
        repository.deleteAll();
        jdbc.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS uq_user_summary_server_nickname_test "
                        + "ON user_summary(server_name, nickname)");
        jdbc.execute(
                "CREATE TABLE IF NOT EXISTS user_summary_dlt_inbox ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY, dlt_topic VARCHAR(255) NOT NULL, "
                        + "dlt_partition INT NOT NULL, dlt_offset BIGINT NOT NULL, payload BLOB NOT NULL, "
                        + "attempts INT NOT NULL DEFAULT 0, next_attempt_at_ms BIGINT NOT NULL, "
                        + "claim_token VARCHAR(36), claim_until_ms BIGINT, completed_at_ms BIGINT, "
                        + "last_error VARCHAR(255), created_at_ms BIGINT NOT NULL, "
                        + "CONSTRAINT uq_dlt_test UNIQUE(dlt_topic,dlt_partition,dlt_offset))");
        jdbc.update("DELETE FROM user_summary_dlt_inbox");
        rejected.clear();
    }

    @Test
    void activityGradeUsesRevisionAndVerificationWithoutChangingRole() throws Exception {
        sendAndAwaitOffset(
                new UserSummaryConsumerDTO(42L, "graded", null, "test", true, 10L, "EXPERT"));
        var user = repository.findById(42L).orElseThrow();
        user.update(null, null, null, UserGrade.ADMIN);
        repository.save(user);
        sendAndAwaitOffset(
                new UserSummaryConsumerDTO(42L, "old", null, "test", true, 9L, "SENIOR"));
        assertThat(repository.findById(42L).orElseThrow().getActivityGrade()).isEqualTo("EXPERT");
        sendAndAwaitOffset(
                new UserSummaryConsumerDTO(42L, "graded", null, null, false, 11L, "EXPERT"));
        var unverified = repository.findById(42L).orElseThrow();
        assertThat(unverified.getActivityGrade()).isNull();
        assertThat(unverified.getGrade()).isEqualTo(UserGrade.ADMIN);
        sendAndAwaitOffset(
                new UserSummaryConsumerDTO(42L, "graded", null, "test", true, 12L, "EXPERT"));
        assertThat(repository.findById(42L).orElseThrow().getActivityGrade()).isEqualTo("EXPERT");
    }

    @Test
    void appliesOnlyNewerRevisionsAndPreservesCommunityFields() throws Exception {
        sendAndAwaitOffset(new UserSummaryConsumerDTO(42L, "first", null, null, false, 2L));
        var saved = repository.findById(42L).orElseThrow();
        saved.update(null, 27, null, UserGrade.ADMIN);
        repository.save(saved);

        sendAndAwaitOffset(new UserSummaryConsumerDTO(42L, "old", "old-image", "server", true, 1L));
        sendAndAwaitOffset(new UserSummaryConsumerDTO(42L, "legacy", null, "server"));
        sendAndAwaitOffset(new UserSummaryConsumerDTO(42L, "duplicate", null, null, false, 2L));
        assertThat(repository.findById(42L).orElseThrow().getNickname()).isEqualTo("first");

        sendAndAwaitOffset(new UserSummaryConsumerDTO(42L, "new", null, null, true, 3L));
        var updated = repository.findById(42L).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("new");
        assertThat(updated.getProfileImageUrl()).isNull();
        assertThat(updated.getServerName()).isNull();
        assertThat(updated.isVerified()).isTrue();
        assertThat(updated.getLastAuthSyncRevision()).isEqualTo(3L);
        assertThat(updated.getLevel()).isEqualTo(27);
        assertThat(updated.getGrade()).isEqualTo(UserGrade.ADMIN);
        assertThat(rejected).isEmpty();
    }

    @Test
    void uniqueNicknameConflictDoesNotChangeAnotherUserAndRetriesFromInbox() throws Exception {
        sendAndAwaitOffset(new UserSummaryConsumerDTO(1L, "occupied", null, "server", true, 1L));
        byte[] transfer =
                new ObjectMapper()
                        .writeValueAsBytes(
                                new UserSummaryConsumerDTO(
                                        2L, "occupied", null, "server", true, 2L));
        inbox.store("USER_INFO_UPDATE_EVENT.DLT", 0, 123L, transfer);
        inbox.store("USER_INFO_UPDATE_EVENT.DLT", 0, 123L, transfer);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT COUNT(*) FROM user_summary_dlt_inbox", Integer.class))
                .isEqualTo(1);

        inbox.retryDue();
        assertThat(repository.findById(1L).orElseThrow().getNickname()).isEqualTo("occupied");
        assertThat(repository.findById(2L)).isEmpty();
        assertThat(
                        jdbc.queryForObject(
                                "SELECT attempts FROM user_summary_dlt_inbox", Integer.class))
                .isEqualTo(1);

        sendAndAwaitOffset(new UserSummaryConsumerDTO(1L, "released", null, "server", true, 2L));
        jdbc.update("UPDATE user_summary_dlt_inbox SET next_attempt_at_ms = 0");
        inbox.retryDue();
        assertThat(repository.findById(2L).orElseThrow().getNickname()).isEqualTo("occupied");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT completed_at_ms FROM user_summary_dlt_inbox", Long.class))
                .isNotNull();
    }

    @Test
    void unresolvedInboxReportsCountAndOldestAgeWithoutPayload(CapturedOutput output)
            throws Exception {
        inbox.store(
                "USER_INFO_UPDATE_EVENT",
                0,
                73L,
                "private-payload".getBytes(StandardCharsets.UTF_8));
        jdbc.update("UPDATE user_summary_dlt_inbox SET created_at_ms = 0");

        inbox.reportPending();

        assertThat(output).contains("USER_SUMMARY_DLT_UNRESOLVED pending=1 oldest_age_ms=");
        assertThat(output).doesNotContain("private-payload");
    }

    @Test
    void createsRepeatsAndUpdatesOnlyAuthFieldsIncludingNullSnapshots() throws Exception {
        repository.save(
                UserSummary.builder()
                        .id(999L)
                        .nickname("sentinel")
                        .serverName("server")
                        .level(23)
                        .grade(UserGrade.ADMIN)
                        .build());

        var signup = new UserSummaryConsumerDTO(42L, "member", null, null);
        sendAndAwaitOffset(signup);
        var created = repository.findById(42L).orElseThrow();
        assertThat(created.getServerName()).isNull();
        assertThat(created.getLevel()).isEqualTo(1);
        assertThat(created.getGrade()).isEqualTo(UserGrade.USER);

        created.update(null, 17, null, UserGrade.ADMIN);
        repository.save(created);
        sendAndAwaitOffset(signup);
        sendAndAwaitOffset(
                new UserSummaryConsumerDTO(
                        42L, "renamed", "https://example.test/profile.png", "server"));
        var updated = repository.findById(42L).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("renamed");
        assertThat(updated.getProfileImageUrl()).isEqualTo("https://example.test/profile.png");
        assertThat(updated.getServerName()).isEqualTo("server");
        assertThat(updated.getLevel()).isEqualTo(17);
        assertThat(updated.getGrade()).isEqualTo(UserGrade.ADMIN);

        sendAndAwaitOffset(new UserSummaryConsumerDTO(42L, "cleared", null, null));
        var cleared = repository.findById(42L).orElseThrow();
        assertThat(cleared.getProfileImageUrl()).isNull();
        assertThat(cleared.getServerName()).isNull();
        assertThat(cleared.getLevel()).isEqualTo(17);
        assertThat(cleared.getGrade()).isEqualTo(UserGrade.ADMIN);
        assertThat(repository.count()).isEqualTo(2);
        var sentinel = repository.findById(999L).orElseThrow();
        assertThat(sentinel.getNickname()).isEqualTo("sentinel");
        assertThat(sentinel.getLevel()).isEqualTo(23);
        assertThat(sentinel.getGrade()).isEqualTo(UserGrade.ADMIN);
        assertThat(rejected).isEmpty();
    }

    static Stream<UserSummaryConsumerDTO> invalidEvents() {
        return Stream.of(
                new UserSummaryConsumerDTO(null, "member", null, "server"),
                new UserSummaryConsumerDTO(43L, null, null, "server"),
                new UserSummaryConsumerDTO(43L, "", null, "server"),
                new UserSummaryConsumerDTO(43L, " \t\n", null, "server"),
                new UserSummaryConsumerDTO(43L, "member", null, ""),
                new UserSummaryConsumerDTO(43L, "member", null, " \t\n"),
                new UserSummaryConsumerDTO(43L, "member", null, "\u2003"));
    }

    @ParameterizedTest
    @MethodSource("invalidEvents")
    void actualListenerRejectsInvalidFieldsBeforePersistence(UserSummaryConsumerDTO invalid)
            throws Exception {
        sendAndAwaitOffset(invalid);
        Exception failure = rejected.poll(5, TimeUnit.SECONDS);
        assertThat(failure).isNotNull();
        assertThat(failure)
                .hasRootCauseInstanceOf(jakarta.validation.ConstraintViolationException.class);
        assertThat(repository.count()).isZero();
    }

    @Test
    void malformedJsonIsRejectedBeforePersistence() throws Exception {
        sendRawAndAwaitOffset("{".getBytes(StandardCharsets.UTF_8));
        assertThat(rejected.poll(5, TimeUnit.SECONDS)).isNotNull();
        assertThat(repository.count()).isZero();
    }

    private void sendAndAwaitOffset(UserSummaryConsumerDTO event) throws Exception {
        sendRawAndAwaitOffset(new ObjectMapper().writeValueAsBytes(event));
    }

    private void sendRawAndAwaitOffset(byte[] payload) throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        var headers =
                new RecordHeaders()
                        .add(
                                "__TypeId__",
                                "until.the.eternity.das.user.dto.response.UserInfoUpdateEvent"
                                        .getBytes(StandardCharsets.UTF_8));
        try (var producer = new KafkaProducer<String, byte[]>(properties);
                var admin =
                        AdminClient.create(
                                Map.of(
                                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                        broker.getBrokersAsString()))) {
            var sent =
                    producer.send(new ProducerRecord<>(TOPIC, 0, null, null, payload, headers))
                            .get(10, TimeUnit.SECONDS);
            await().atMost(Duration.ofSeconds(15))
                    .untilAsserted(
                            () -> {
                                var offset =
                                        admin.listConsumerGroupOffsets(GROUP)
                                                .partitionsToOffsetAndMetadata()
                                                .get(5, TimeUnit.SECONDS)
                                                .get(new TopicPartition(TOPIC, 0));
                                assertThat(offset).isNotNull();
                                assertThat(offset.offset()).isGreaterThan(sent.offset());
                            });
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableKafka
    @EntityScan(basePackageClasses = UserSummary.class)
    @EnableJpaRepositories(basePackageClasses = UserSummaryRepository.class)
    @Import({
        KafkaConsumerService.class,
        UserSummaryProjectionService.class,
        UserSummaryDltInboxService.class
    })
    static class Config {
        @Bean
        BlockingQueue<Exception> rejected() {
            return new LinkedBlockingQueue<>();
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactory(
                EmbeddedKafkaBroker broker, BlockingQueue<Exception> rejected) throws Exception {
            var properties = new HashMap<>(MainKafkaConsumerProperties.forProfile("local"));
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            var factory = new ConcurrentKafkaListenerContainerFactory<String, byte[]>();
            factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties));
            factory.getContainerProperties().setPollTimeout(100);
            factory.setCommonErrorHandler(
                    new DefaultErrorHandler(
                            (record, exception) -> rejected.add(exception),
                            new FixedBackOff(0, 0)));
            return factory;
        }
    }
}
