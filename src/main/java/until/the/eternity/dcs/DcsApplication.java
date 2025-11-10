package until.the.eternity.dcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {org.redisson.spring.starter.RedissonAutoConfigurationV2.class})
@EnableJpaAuditing
@EnableScheduling
public class DcsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DcsApplication.class, args);
    }
}
