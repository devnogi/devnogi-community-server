package until.the.eternity.dcs.domain.outfit;

import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Uses only explicitly provisioned disposable localhost MySQL schemas. */
@Tag("outfit-mysql")
class OutfitMysqlIntegrationTest extends OutfitServiceTest {
    private static DataSource mysql;

    @BeforeAll
    static void migrateFreshAndUpgrade() {
        String url = System.getenv("TEST_MYSQL_URL");
        assertNotNull(url, "TEST_MYSQL_URL must target isolated localhost MySQL");
        assertTrue(
                url.startsWith("jdbc:mysql://127.0.0.1:")
                        || url.startsWith("jdbc:mysql://localhost:"),
                "Never run destructive integration setup against a remote server");
        String user = System.getenv("TEST_MYSQL_USER");
        String password = System.getenv("TEST_MYSQL_PASSWORD");
        assertNotNull(user);
        assertNotNull(password);
        for (String schema : new String[] {"outfit_snap_fresh_test", "outfit_snap_upgrade_test"}) {
            Flyway baseline =
                    Flyway.configure()
                            .dataSource(url, user, password)
                            .schemas(schema)
                            .defaultSchema(schema)
                            .cleanDisabled(false)
                            .locations("classpath:db/migration")
                            .target(schema.contains("upgrade") ? "20261105000002" : "latest")
                            .load();
            baseline.clean();
            baseline.migrate();
            if (schema.contains("upgrade")) {
                new JdbcTemplate(
                                new DriverManagerDataSource(schemaUrl(url, schema), user, password))
                        .update(
                                "INSERT INTO user_summary(id,nickname,is_verified) VALUES(99999,'migration-survivor',false)");
            }
            Flyway complete =
                    Flyway.configure()
                            .dataSource(url, user, password)
                            .schemas(schema)
                            .defaultSchema(schema)
                            .locations("classpath:db/migration")
                            .load();
            complete.migrate();
            complete.validate();
            JdbcTemplate verify =
                    new JdbcTemplate(
                            new DriverManagerDataSource(schemaUrl(url, schema), user, password));
            assertEquals(
                    8,
                    verify.queryForObject(
                            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=? AND table_name LIKE 'outfit\\_%'",
                            Integer.class, schema));
            if (schema.contains("upgrade"))
                assertEquals(
                        "migration-survivor",
                        verify.queryForObject(
                                "SELECT nickname FROM user_summary WHERE id=99999", String.class));
        }
        mysql =
                new DriverManagerDataSource(
                        schemaUrl(url, "outfit_snap_upgrade_test"), user, password);
    }

    private static String schemaUrl(String url, String schema) {
        int slash = url.indexOf('/', "jdbc:mysql://".length());
        int query = url.indexOf('?');
        return url.substring(0, slash + 1) + schema + (query < 0 ? "" : url.substring(query));
    }

    @Override
    protected DataSource dataSource() {
        return mysql;
    }
}
