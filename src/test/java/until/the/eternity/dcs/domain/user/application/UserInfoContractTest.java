package until.the.eternity.dcs.domain.user.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;

class UserInfoContractTest {
    static Stream<Arguments> profilesAndHeaders() {
        return Stream.of("local", "prod", "docker")
                .flatMap(
                        profile ->
                                Stream.of(
                                        Arguments.of(profile, true), Arguments.of(profile, false)));
    }

    @ParameterizedTest
    @MethodSource("profilesAndHeaders")
    void readsAuthFixtureUsingActualProfileSettings(String profile, boolean hasProducerHeader)
            throws Exception {
        byte[] payload =
                Files.readAllBytes(Path.of("src/test/resources/contracts/user-info-update.json"));
        var properties = MainKafkaConsumerProperties.forProfile(profile);
        assertThat(properties.get("spring.json.use.type.headers")).isEqualTo("false");
        assertThat(properties.get("spring.json.value.default.type"))
                .isEqualTo(UserSummaryConsumerDTO.class.getName());
        assertThat(properties.get("spring.json.trusted.packages")).isEqualTo("*");
        assertThat(properties.get("group.id")).isEqualTo("devnogi-community-group");
        assertThat(properties.get("auto.offset.reset")).isEqualTo("latest");
        var headers = new RecordHeaders();
        if (hasProducerHeader) {
            headers.add(
                    "__TypeId__",
                    "until.the.eternity.das.user.dto.response.UserInfoUpdateEvent"
                            .getBytes(StandardCharsets.UTF_8));
        }
        try (var deserializer = new JsonDeserializer<UserSummaryConsumerDTO>()) {
            deserializer.configure(properties, false);
            var user = deserializer.deserialize("USER_INFO_UPDATE_EVENT", headers, payload);
            assertThat(user.id()).isEqualTo(42L);
            assertThat(user.nickname()).isEqualTo("contract-user");
            assertThat(user.profileImageUrl()).isNull();
            assertThat(user.serverName()).isNull();
        }
    }
}
