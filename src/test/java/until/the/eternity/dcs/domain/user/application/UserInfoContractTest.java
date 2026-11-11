package until.the.eternity.dcs.domain.user.application;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.Column;
import jakarta.validation.Validation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

class UserInfoContractTest {
    @Test
    void readsNewProducerExplicitNullAndRetainsMissingLegacyPresence() throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var upgraded =
                mapper.readValue(
                        Files.readString(
                                Path.of(
                                        "src/test/resources/contracts/user-info-update-arcana.json")),
                        UserSummaryConsumerDTO.class);
        var legacy =
                mapper.readValue(
                        Files.readString(
                                Path.of("src/test/resources/contracts/user-info-update.json")),
                        UserSummaryConsumerDTO.class);
        assertTrue(upgraded.hasArcana());
        assertNull(upgraded.arcana());
        assertFalse(legacy.hasArcana());
    }

    @Test
    void readsAuthProducerFixture() throws Exception {
        byte[] payload =
                Files.readAllBytes(Path.of("src/test/resources/contracts/user-info-update.json"));
        try (var deserializer = new JsonDeserializer<>(UserSummaryConsumerDTO.class, false)) {
            var user = deserializer.deserialize("USER_INFO_UPDATE_EVENT", payload);
            assertEquals(42L, user.id());
            assertEquals("contract-user", user.nickname());
            assertNull(user.profileImageUrl());
            assertNull(user.serverName());
            assertFalse(user.verified());
            assertNull(user.revision());
            assertEquals(user.id(), user.toEntity().getId());
        }
    }

    @Test
    void acceptsUnverifiedUserWithoutServerName() throws Exception {
        byte[] payload =
                Files.readAllBytes(Path.of("src/test/resources/contracts/user-info-update.json"));
        try (var deserializer = new JsonDeserializer<>(UserSummaryConsumerDTO.class, false);
                var factory = Validation.buildDefaultValidatorFactory()) {
            var user = deserializer.deserialize("USER_INFO_UPDATE_EVENT", payload);
            var violations = factory.getValidator().validate(user);
            assertTrue(violations.isEmpty());
            assertNull(user.toEntity().getServerName());
            assertTrue(
                    UserSummary.class
                            .getDeclaredField("serverName")
                            .getAnnotation(Column.class)
                            .nullable());
            assertFalse(
                    factory.getValidator()
                            .validate(new UserSummaryConsumerDTO(42L, "name", null, "  ", false))
                            .isEmpty());
            assertFalse(
                    factory.getValidator()
                            .validate(
                                    new UserSummaryConsumerDTO(42L, "name", null, null, false, 0L))
                            .isEmpty());
        }
    }

    @Test
    void mapsVerifiedStateFromAuthEvent() {
        UserSummary user = new UserSummaryConsumerDTO(42L, "name", null, "류트", true).toEntity();

        assertTrue(user.isVerified());
    }

    @Test
    void mainKafkaProfilesUseHeaderIndependentDeserializerAndDltProducer() throws Exception {
        for (String profile : List.of("local", "prod", "docker")) {
            var consumer = MainKafkaConsumerProperties.forProfile(profile);
            var producer = MainKafkaConsumerProperties.producerForProfile(profile);

            assertEquals(ByteArrayDeserializer.class, consumer.get("value.deserializer"));
            assertEquals(StringSerializer.class, producer.get("key.serializer"));
            assertEquals(ByteArraySerializer.class, producer.get("value.serializer"));
        }
    }
}
