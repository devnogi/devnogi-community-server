package until.the.eternity.dcs.domain.guild;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import until.the.eternity.dcs.common.infrastructure.MinioService;

@Tag("guild-mysql")
class GuildContentMysqlTest {
    static JdbcTemplate db;
    static TransactionTemplate tx;
    GuildContent content;
    GuildApplications applications;
    static final long A = 951001, B = 951002, APPLICANT = 951003;
    int first, second;

    @BeforeAll
    static void createSchema() {
        String url = Objects.requireNonNull(System.getenv("TEST_MYSQL_URL"));
        assertTrue(
                url.startsWith("jdbc:mysql://127.0.0.1:")
                        || url.startsWith("jdbc:mysql://localhost:"));
        String schema = "guild_content_test_" + UUID.randomUUID().toString().replace("-", "");
        String user = System.getenv("TEST_MYSQL_USER"), pass = System.getenv("TEST_MYSQL_PASSWORD");
        int slash = url.indexOf('/', "jdbc:mysql://".length()), query = url.indexOf('?');
        Flyway.configure()
                .dataSource(url, user, pass)
                .schemas(schema)
                .defaultSchema(schema)
                .load()
                .migrate();
        var ds =
                new DriverManagerDataSource(
                        url.substring(0, slash + 1)
                                + schema
                                + (query < 0 ? "" : url.substring(query)),
                        user,
                        pass);
        db = new JdbcTemplate(ds);
        tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
        for (long id : new long[] {A, B, APPLICANT})
            db.update(
                    "INSERT INTO user_summary(id,nickname,server_name,is_verified) VALUES(?,?,?,true)",
                    id,
                    "test" + id,
                    "류트");
    }

    @BeforeEach
    void setUp() {
        GuildAccess access =
                new GuildAccess(db, "http://localhost") {
                    @Override
                    public Identity verified(long user) {
                        return new Identity("류트", "캐릭터" + user);
                    }
                };
        content = new GuildContent(db, access);
        applications = new GuildApplications(db, access, content);
        for (String table :
                List.of(
                        "guild_join_application",
                        "guild_recruitment_post",
                        "guild_post",
                        "guild_profile",
                        "guild_registration_review",
                        "guild_registration_request",
                        "guild_member",
                        "guild")) db.update("DELETE FROM " + table);
        first = guild(A);
        second = guild(B);
    }

    int guild(long user) {
        return tx.execute(
                s -> {
                    db.update(
                            "INSERT INTO guild(name,server,register_id,master_id) VALUES(?,'류트',?,?)",
                            "guild" + user,
                            user,
                            user);
                    int id = db.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
                    db.update("INSERT INTO guild_profile(guild_id) VALUES(?)", id);
                    db.update(
                            "INSERT INTO guild_member(guild_id,user_id,role) VALUES(?,?,'MASTER')",
                            id,
                            user);
                    return id;
                });
    }

    <T> T act(long user, Supplier<T> callback) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        try {
            return tx.execute(s -> callback.get());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    long recruit(int guild, long owner) {
        return act(
                owner,
                () ->
                        content.save(
                                        guild,
                                        null,
                                        false,
                                        new GuildContent.Writing("모집", "소개", null, null))
                                .get("id"));
    }

    long apply(int guild, long recruit) {
        return act(APPLICANT, () -> applications.apply(guild, recruit, "지원").get("id"));
    }

    @Test
    void registerCustomizeAppointRecruitAcceptAndWrite() {
        var registry = new GuildRegistryController(new GuildRegistry(db, content.access));
        var api =
                new GuildContentController(
                        content, new GuildMedia(content, mock(MinioService.class)), applications);
        act(
                A,
                () -> {
                    registry.leave(first);
                    registry.submit(new GuildRegistryController.Registration("우리길드", "길드 소개"));
                    return null;
                });
        act(
                B,
                () -> {
                    registry.leave(second);
                    return null;
                });
        assertTrue(api.list("류트", "우리길드", false, 0).isEmpty());
        long requestId = ((Number) act(A, registry::mine).getFirst().get("id")).longValue();
        int g =
                act(
                        B,
                        () -> {
                            admin(B);
                            registry.review(
                                    requestId, new GuildRegistryController.Review("APPROVED", ""));
                            return ((Number) registry.queue().getFirst().get("guildId")).intValue();
                        });
        assertNull(api.home(g).get("masterId"));
        act(
                A,
                () -> {
                    api.profile(g, new GuildContent.Profile("어서오세요", "우리 이야기", "SKY", "친목", "저녁"));
                    return null;
                });
        assertEquals("어서오세요", api.home(g).get("tagline"));
        assertThrows(ResponseStatusException.class, () -> recruit(g, A));
        act(
                B,
                () -> {
                    admin(B);
                    registry.master(g, new GuildRegistryController.Master(B));
                    return null;
                });
        act(
                B,
                () -> {
                    registry.role(g, A, new GuildRegistryController.Role("DEPUTY"));
                    return null;
                });
        long r =
                act(
                        A,
                        () ->
                                api.create(
                                                g,
                                                "recruitments",
                                                new GuildContent.Writing(
                                                        "모집", "환영합니다", null, "같은 서버"))
                                        .get("id"));
        long application =
                act(
                        APPLICANT,
                        () ->
                                api.apply(
                                                g,
                                                r,
                                                new GuildContentController.ApplicationRequest(
                                                        "지원합니다"))
                                        .get("id"));
        act(
                A,
                () -> {
                    api.decide(g, r, application, "accept");
                    return null;
                });
        long post =
                act(
                        APPLICANT,
                        () ->
                                api.create(
                                                g,
                                                "posts",
                                                new GuildContent.Writing(
                                                        "첫 모임", "단체 사진", null, null))
                                        .get("id"));
        assertEquals("MEMBERS", act(B, () -> api.writing(g, "posts", post)).get("visibility"));
        assertTrue(api.writings(g, "posts", 0).isEmpty());
    }

    void admin(long user) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    void privatePostsExcludePreviewDetailAndMedia() {
        long id =
                act(
                        A,
                        () ->
                                content.save(
                                                first,
                                                null,
                                                true,
                                                new GuildContent.Writing("추억", "내용", null, null))
                                        .get("id"));
        assertTrue(content.writings(first, true, 0).isEmpty());
        assertTrue(((List<?>) content.home(first).get("posts")).isEmpty());
        assertThrows(ResponseStatusException.class, () -> content.writing(first, id, true));
        var storage = mock(MinioService.class);
        assertThrows(
                ResponseStatusException.class,
                () -> new GuildMedia(content, storage).image(first, id, null));
        verifyNoInteractions(storage);
        assertEquals("추억", act(A, () -> content.writing(first, id, true)).get("title"));
        assertThrows(
                ResponseStatusException.class,
                () ->
                        act(
                                B,
                                () ->
                                        content.save(
                                                first,
                                                id,
                                                true,
                                                new GuildContent.Writing(
                                                        "교체", "본문", "PUBLIC", null))));
    }

    @Test
    void competingAcceptancesCommitOnlyOneMembership() throws Exception {
        long r1 = recruit(first, A), r2 = recruit(second, B);
        long a1 = apply(first, r1), a2 = apply(second, r2);
        CyclicBarrier start = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var x = pool.submit(() -> accept(start, A, first, r1, a1));
            var y = pool.submit(() -> accept(start, B, second, r2, a2));
            assertNotEquals(x.get(15, TimeUnit.SECONDS), y.get(15, TimeUnit.SECONDS));
        }
        assertEquals(
                1,
                db.queryForObject(
                        "SELECT COUNT(*) FROM guild_member WHERE active_user=?",
                        Integer.class,
                        APPLICANT));
        assertEquals(
                List.of("ACCEPTED", "CANCELLED"),
                db
                        .queryForList(
                                "SELECT status FROM guild_join_application ORDER BY status+0",
                                String.class)
                        .stream()
                        .sorted()
                        .toList());
    }

    boolean accept(CyclicBarrier start, long owner, int guild, long r, long app) throws Exception {
        start.await(5, TimeUnit.SECONDS);
        try {
            act(
                    owner,
                    () -> {
                        applications.decide(guild, r, app, "accept");
                        return null;
                    });
            return true;
        } catch (ResponseStatusException e) {
            assertEquals(409, e.getStatusCode().value());
            return false;
        }
    }

    @Test
    void closeAndAcceptSerialize() throws Exception {
        long r = recruit(first, A), a = apply(first, r);
        CyclicBarrier start = new CyclicBarrier(2);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var acceptance = pool.submit(() -> accept(start, A, first, r, a));
            var closure =
                    pool.submit(
                            () -> {
                                start.await(5, TimeUnit.SECONDS);
                                act(
                                        A,
                                        () -> {
                                            content.remove(first, r, false, false);
                                            return null;
                                        });
                                return true;
                            });
            closure.get(15, TimeUnit.SECONDS);
            boolean joined = acceptance.get(15, TimeUnit.SECONDS);
            assertEquals(
                    joined ? "ACCEPTED" : "CANCELLED",
                    db.queryForObject(
                            "SELECT status FROM guild_join_application WHERE id=?",
                            String.class,
                            a));
            assertEquals(
                    joined ? 1 : 0,
                    db.queryForObject(
                            "SELECT COUNT(*) FROM guild_member WHERE active_user=?",
                            Integer.class,
                            APPLICANT));
        }
    }
}
