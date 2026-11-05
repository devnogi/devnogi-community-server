package until.the.eternity.dcs.domain.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import until.the.eternity.dcs.common.config.KafkaConfig;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@SpringBootTest(
        classes = UserSummaryDltWiringIntegrationTest.Config.class,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:user-summary-dlt-wiring;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.show_sql=false",
            "spring.flyway.enabled=false",
            "spring.docker.compose.enabled=false",
            "spring.kafka.consumer.group-id=devnogi-community-group",
            "spring.kafka.consumer.auto-offset-reset=earliest",
            "spring.kafka.consumer.enable-auto-commit=false",
            "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
            "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer",
            "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
            "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.ByteArraySerializer",
            "logging.level.org.apache.kafka=WARN",
            "logging.level.kafka=WARN",
            "logging.level.state.change.logger=WARN"
        })
@EmbeddedKafka(
        partitions = 1,
        topics = {
            UserSummaryDltWiringIntegrationTest.TOPIC,
            UserSummaryDltWiringIntegrationTest.DLT_TOPIC
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@DirtiesContext
class UserSummaryDltWiringIntegrationTest {
    static final String TOPIC = "USER_INFO_UPDATE_EVENT";
    static final String DLT_TOPIC = TOPIC + ".DLT";
    private static final String MAIN_GROUP = "devnogi-community-group";
    private static final String DLT_GROUP = "devnogi-community-user-summary-dlt";

    @Autowired private EmbeddedKafkaBroker broker;
    @Autowired private KafkaTemplate<Object, byte[]> kafkaTemplate;
    @Autowired private KafkaListenerEndpointRegistry registry;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void prepareInboxAndListeners() {
        jdbc.execute(
                "CREATE TABLE IF NOT EXISTS user_summary_dlt_inbox ("
                        + "id BIGINT AUTO_INCREMENT PRIMARY KEY, dlt_topic VARCHAR(255) NOT NULL, "
                        + "dlt_partition INT NOT NULL, dlt_offset BIGINT NOT NULL, payload BLOB NOT NULL, "
                        + "attempts INT NOT NULL DEFAULT 0, next_attempt_at_ms BIGINT NOT NULL, "
                        + "claim_token VARCHAR(36), claim_until_ms BIGINT, completed_at_ms BIGINT, "
                        + "last_error VARCHAR(255), created_at_ms BIGINT NOT NULL, "
                        + "CONSTRAINT uq_dlt_wiring UNIQUE(dlt_topic,dlt_partition,dlt_offset))");
        jdbc.update("DELETE FROM user_summary_dlt_inbox");
        registry.getListenerContainers()
                .forEach(container -> ContainerTestUtils.waitForAssignment(container, 1));
    }

    @Test
    void malformedMainEventFlowsThroughProductionRecovererIntoDurableInbox() throws Exception {
        byte[] malformed = "{".getBytes(StandardCharsets.UTF_8);

        var sent = kafkaTemplate.send(TOPIC, "42", malformed).get(10, TimeUnit.SECONDS);

        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(
                        () -> {
                            var rows =
                                    jdbc.queryForList(
                                            "SELECT dlt_topic, dlt_partition, dlt_offset, payload "
                                                    + "FROM user_summary_dlt_inbox");
                            assertThat(rows).hasSize(1);
                            var stored = rows.get(0);
                            assertThat(stored.get("DLT_TOPIC")).isEqualTo(TOPIC);
                            assertThat(stored.get("DLT_PARTITION")).isEqualTo(0);
                            assertThat(((Number) stored.get("DLT_OFFSET")).longValue())
                                    .isEqualTo(sent.getRecordMetadata().offset());
                            assertThat((byte[]) stored.get("PAYLOAD")).isEqualTo(malformed);
                            assertThat(committedOffset(MAIN_GROUP, TOPIC)).isGreaterThan(0L);
                            assertThat(committedOffset(DLT_GROUP, DLT_TOPIC)).isGreaterThan(0L);
                        });
    }

    private long committedOffset(String group, String topic) throws Exception {
        try (var admin =
                AdminClient.create(
                        Map.of(
                                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                broker.getBrokersAsString()))) {
            Map<TopicPartition, OffsetAndMetadata> offsets =
                    admin.listConsumerGroupOffsets(group)
                            .partitionsToOffsetAndMetadata()
                            .get(5, TimeUnit.SECONDS);
            OffsetAndMetadata offset = offsets.get(new TopicPartition(topic, 0));
            return offset == null ? -1L : offset.offset();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableKafka
    @EntityScan(basePackageClasses = UserSummary.class)
    @EnableJpaRepositories(basePackageClasses = UserSummaryRepository.class)
    @Import({
        KafkaConfig.class,
        KafkaConsumerService.class,
        UserSummaryProjectionService.class,
        UserSummaryDltInboxService.class,
        UserSummaryDltListener.class
    })
    static class Config {}
}
