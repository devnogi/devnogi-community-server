package until.the.eternity.dcs.domain.experience;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Separate entrypoint: no web server, application schedulers, listeners, or Flyway mutations. */
@Slf4j
public final class ActivityBackfillCommand {
    private ActivityBackfillCommand() {}

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackfillConfiguration.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (var context = app.run(args)) {
            var env = context.getEnvironment();
            if (!env.getProperty("experience.backfill.command-enabled", Boolean.class, false))
                throw new IllegalStateException(
                        "Explicit experience.backfill.command-enabled=true required");
            var backfill = context.getBean(ActivityBackfill.class);
            switch (env.getProperty("experience.backfill.operation", "dry-run")) {
                case "freeze" ->
                        backfill.freeze(
                                Instant.parse(env.getRequiredProperty("experience.backfill.cutoff"))
                                        .truncatedTo(ChronoUnit.MICROS));
                case "run" -> {
                    int size =
                            env.getProperty("experience.backfill.batch-size", Integer.class, 100);
                    for (ActivitySource source : ActivitySource.values())
                        backfill.step(source, size);
                }
                case "dry-run" -> {}
                default ->
                        throw new IllegalArgumentException(
                                "Operation must be freeze, dry-run, or run");
            }
            for (var progress : backfill.dryRun()) log.info("Activity backfill: {}", progress);
        }
    }

    @ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        TransactionAutoConfiguration.class
    })
    @EnableTransactionManagement
    @Import({ActivityBackfill.class, ActivityOutbox.class})
    static class BackfillConfiguration {}
}
