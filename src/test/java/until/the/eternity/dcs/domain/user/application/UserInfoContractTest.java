package until.the.eternity.dcs.domain.user.application;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.Column;
import jakarta.validation.Validation;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

class UserInfoContractTest {
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
                            .validate(new UserSummaryConsumerDTO(42L, "name", null, "  "))
                            .isEmpty());
        }
    }
}
