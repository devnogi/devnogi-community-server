package until.the.eternity.dcs.domain.guild;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Tag("guild-mysql")
class GuildRegistryMysqlTest {
    @Test
    void simultaneousReviewsCreateOneGuildWithVacantMasterAndRegisteredMember() throws Exception {
        String url = System.getenv("TEST_MYSQL_URL");
        assertNotNull(url);
        assertTrue(
                url.startsWith("jdbc:mysql://127.0.0.1:")
                        || url.startsWith("jdbc:mysql://localhost:"));
        String schema = "guild_registry_upgrade_test",
                user = System.getenv("TEST_MYSQL_USER"),
                password = System.getenv("TEST_MYSQL_PASSWORD");
        var flyway =
                Flyway.configure()
                        .dataSource(url, user, password)
                        .schemas(schema)
                        .defaultSchema(schema)
                        .cleanDisabled(false)
                        .target("20261107000001")
                        .load();
        flyway.clean();
        flyway.migrate();
        int slash = url.indexOf('/', "jdbc:mysql://".length()), query = url.indexOf('?');
        var ds =
                new DriverManagerDataSource(
                        url.substring(0, slash + 1)
                                + schema
                                + (query < 0 ? "" : url.substring(query)),
                        user,
                        password);
        var jdbc = new JdbcTemplate(ds);
        jdbc.update(
                "INSERT INTO user_summary(id,nickname,is_verified,server_name) VALUES(91001,'registrant',true,'류트'),(91002,'admin',false,null)");
        Flyway.configure()
                .dataSource(url, user, password)
                .schemas(schema)
                .defaultSchema(schema)
                .load()
                .migrate();
        var tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
        var access = spy(new GuildAccess(jdbc, "http://localhost"));
        doReturn(91001L).when(access).user();
        doReturn(new GuildAccess.Identity("류트", "registrant")).when(access).verified(91001L);
        var registry = new GuildRegistry(jdbc, access);
        tx.executeWithoutResult(s -> registry.submit("길드", "신청"));
        long request = jdbc.queryForObject("SELECT id FROM guild_registration_request", Long.class);
        doNothing().when(access).requireAdmin();
        doReturn(91002L).when(access).user();
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> approve =
                    () -> {
                        assertTrue(start.await(10, TimeUnit.SECONDS));
                        try {
                            tx.executeWithoutResult(s -> registry.review(request, "APPROVED", ""));
                            return true;
                        } catch (ResponseStatusException e) {
                            assertEquals(409, e.getStatusCode().value());
                            return false;
                        }
                    };
            Future<Boolean> first = pool.submit(approve);
            Future<Boolean> second = pool.submit(approve);
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        }
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM guild WHERE master_id IS NULL AND register_id=91001",
                        Integer.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM guild_registration_review", Integer.class));
        assertEquals(
                "MEMBER",
                jdbc.queryForObject(
                        "SELECT role FROM guild_member WHERE active_user=91001", String.class));
        int guild = jdbc.queryForObject("SELECT id FROM guild", Integer.class);
        tx.executeWithoutResult(s -> registry.master(guild, 91001));
        assertEquals("MASTER", access.role(guild, 91001));
        doReturn(91001L).when(access).user();
        tx.executeWithoutResult(s -> registry.leave(guild));
        assertNull(access.role(guild, 91001));
        assertNull(
                jdbc.queryForObject("SELECT master_id FROM guild WHERE id=?", Long.class, guild));
    }
}
