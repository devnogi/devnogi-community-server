package until.the.eternity.dcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DcsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DcsApplication.class, args);
	}

}
