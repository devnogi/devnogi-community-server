package until.the.eternity.dcs.domain.user.application;

import java.io.IOException;
import java.util.Map;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;

/**
 * Read production resources explicitly because test application.yml shadows the classpath resource.
 */
final class MainKafkaConsumerProperties {
    private MainKafkaConsumerProperties() {}

    static Map<String, Object> forProfile(String profile) throws IOException {
        var sources =
                new YamlPropertySourceLoader()
                        .load("main", new FileSystemResource("src/main/resources/application.yml"));
        var source =
                sources.stream()
                        .filter(
                                candidate ->
                                        profile.equals(
                                                candidate.getProperty(
                                                        "spring.config.activate.on-profile")))
                        .findFirst()
                        .orElseThrow();
        var environment = new StandardEnvironment();
        environment
                .getPropertySources()
                .remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment
                .getPropertySources()
                .remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().addFirst(source);
        return Binder.get(environment)
                .bind("spring.kafka", KafkaProperties.class)
                .orElseThrow(IllegalStateException::new)
                .buildConsumerProperties(null);
    }
}
