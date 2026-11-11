package until.the.eternity.dcs.domain.experience;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Tag("experience-mysql")
class ActivityMysqlIntegrationTest {
    @Test
    void arcanaProjectionPreservesLegacyMissingAndClearsExplicitNullOnMysql() {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var projection =
                    new until.the.eternity.dcs.domain.user.application.UserSummaryProjectionService(
                            db, factory.getValidator());
            var arcana = com.fasterxml.jackson.databind.node.TextNode.valueOf("DARK_MAGE");
            tx.executeWithoutResult(
                    status ->
                            projection.apply(
                                    new until.the.eternity.dcs.domain.user.dto.response
                                            .UserSummaryConsumerDTO(
                                            1L,
                                            "xp-author",
                                            null,
                                            null,
                                            true,
                                            1L,
                                            "EXPERT",
                                            arcana)));
            tx.executeWithoutResult(
                    status ->
                            projection.apply(
                                    new until.the.eternity.dcs.domain.user.dto.response
                                            .UserSummaryConsumerDTO(
                                            1L, "xp-author", null, null, true, 2L, "EXPERT")));
            assertEquals(
                    "DARK_MAGE",
                    db.queryForObject("SELECT arcana FROM user_summary WHERE id=1", String.class));
            tx.executeWithoutResult(
                    status ->
                            projection.apply(
                                    new until.the.eternity.dcs.domain.user.dto.response
                                            .UserSummaryConsumerDTO(
                                            1L,
                                            "xp-author",
                                            null,
                                            null,
                                            true,
                                            3L,
                                            "EXPERT",
                                            com.fasterxml.jackson.databind.node.NullNode
                                                    .instance)));
            assertNull(
                    db.queryForObject("SELECT arcana FROM user_summary WHERE id=1", String.class));
            assertEquals(
                    "ADMIN",
                    db.queryForObject("SELECT grade FROM user_summary WHERE id=1", String.class));
            assertEquals(
                    "EXPERT",
                    db.queryForObject(
                            "SELECT activity_grade FROM user_summary WHERE id=1", String.class));
        }
    }

    static final java.util.TimeZone ORIGINAL_ZONE = java.util.TimeZone.getDefault();
    static JdbcTemplate db;
    static TransactionTemplate tx;
    ActivityOutbox outbox;
    ActivityBackfill backfill;

    @BeforeAll
    static void migrate() {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Seoul"));
        String url = Objects.requireNonNull(System.getenv("TEST_MYSQL_URL"));
        url +=
                (url.contains("?") ? "&" : "?")
                        + "connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true";
        assertTrue(
                url.startsWith("jdbc:mysql://127.0.0.1:")
                        || url.startsWith("jdbc:mysql://localhost:"));
        String schema = "community_xp_test_" + UUID.randomUUID().toString().replace("-", "");
        String user = System.getenv("TEST_MYSQL_USER"),
                password = System.getenv("TEST_MYSQL_PASSWORD");
        var flyway =
                Flyway.configure()
                        .dataSource(url, user, password)
                        .schemas(schema)
                        .defaultSchema(schema)
                        .load();
        flyway.migrate();
        flyway.validate();
        int slash = url.indexOf('/', "jdbc:mysql://".length()), query = url.indexOf('?');
        var ds =
                new DriverManagerDataSource(
                        url.substring(0, slash + 1)
                                + schema
                                + (query < 0 ? "" : url.substring(query)),
                        user,
                        password);
        db = new JdbcTemplate(ds);
        tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
    }

    @AfterAll
    static void restoreZone() {
        java.util.TimeZone.setDefault(ORIGINAL_ZONE);
    }

    @Test
    void utcDatetimeStorageSurvivesSeoulJvmAndUtcConnection() {
        Instant cutoff = Instant.parse("2026-01-02T03:04:05Z");
        tx.executeWithoutResult(
                status -> {
                    outbox.baseline(ActivitySource.POST, 1, 1, 1, 1, cutoff);
                    backfill.freeze(cutoff);
                    backfill.freeze(cutoff);
                });
        assertEquals(
                "2026-01-02 03:04:05",
                db.queryForObject(
                        "SELECT DATE_FORMAT(occurred_at,'%Y-%m-%d %H:%i:%s') FROM activity_outbox",
                        String.class));
        assertEquals(
                "2026-01-02 03:04:05",
                db.queryForObject(
                        "SELECT DATE_FORMAT(cutoff_time,'%Y-%m-%d %H:%i:%s') FROM activity_backfill_checkpoint WHERE source='POST'",
                        String.class));
        var publisher = publisher(mock(KafkaTemplate.class));
        var claimed = tx.execute(status -> publisher.claim());
        assertNotNull(claimed);
        assertEquals(cutoff, claimed.event().occurredAt());
        assertTrue(
                db.queryForObject(
                        "SELECT TIMESTAMPDIFF(SECOND,UTC_TIMESTAMP(),claimed_until) BETWEEN 115 AND 121 FROM activity_outbox",
                        Boolean.class));
        assertNull(tx.execute(status -> publisher.claim()));
        db.update(
                "UPDATE post SET created_at='2026-01-02 03:04:00',first_published_at=NULL WHERE id=1");
        db.update("DELETE FROM activity_outbox");
        assertEquals(1, tx.execute(status -> backfill.step(ActivitySource.POST, 100)).intValue());
        var baseline = tx.execute(status -> publisher.claim());
        assertEquals(Instant.parse("2026-01-02T03:04:00Z"), baseline.event().occurredAt());
    }

    @BeforeEach
    void seed() {
        outbox = new ActivityOutbox(db, true);
        backfill = new ActivityBackfill(db, outbox, "UTC", "UTC");
        tx.executeWithoutResult(
                status -> {
                    for (String table :
                            List.of(
                                    "activity_outbox",
                                    "activity_backfill_checkpoint",
                                    "post_like",
                                    "comment_like")) db.update("DELETE FROM " + table);
                    db.update("DELETE FROM comment WHERE parent_comment_id IS NOT NULL");
                    for (String table :
                            List.of(
                                    "comment",
                                    "post",
                                    "outfit_like",
                                    "outfit_comment",
                                    "outfit_post",
                                    "guild_post",
                                    "guild_recruitment_post",
                                    "guild",
                                    "user_summary")) db.update("DELETE FROM " + table);
                    db.update(
                            "INSERT INTO user_summary(id,nickname,is_verified,grade) VALUES(1,'xp-author',true,'ADMIN'),(2,'xp-actor',true,'USER')");
                    db.update(
                            "INSERT INTO post(id,user_id,title,content,is_draft,is_blocked,is_deleted) VALUES(1,1,'post','body',false,false,false),(2,1,'draft','body',true,false,false),(3,1,'blocked','body',false,true,false),(4,1,'deleted','body',false,false,true)");
                    db.update(
                            "INSERT INTO comment(id,user_id,post_id,content) VALUES(1,1,1,'comment')");
                    db.update(
                            "INSERT INTO comment(id,user_id,post_id,parent_comment_id,content) VALUES(2,2,1,1,'reply')");
                    db.update("INSERT INTO post_like(post_id,user_id) VALUES(1,2),(1,1)");
                    db.update("INSERT INTO comment_like(comment_id,user_id) VALUES(1,2)");
                    db.update(
                            "INSERT INTO outfit_post(id,user_id,content,created_at,updated_at) VALUES(1,1,'outfit',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))");
                    db.update(
                            "INSERT INTO outfit_comment(id,outfit_id,user_id,content,created_at,updated_at) VALUES(1,1,2,'comment',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))");
                    db.update(
                            "INSERT INTO outfit_like(outfit_id,user_id,created_at) VALUES(1,2,UTC_TIMESTAMP(6))");
                    db.update(
                            "INSERT INTO guild(id,name,server,register_id) VALUES(1,'xp-guild','test',1)");
                    db.update(
                            "INSERT INTO guild_post(id,guild_id,author_id,title,body) VALUES(1,1,1,'memory','body')");
                    db.update(
                            "INSERT INTO guild_recruitment_post(id,guild_id,author_id,title,body,status) VALUES(1,1,1,'closed recruitment','body','CLOSED')");
                });
    }

    long count(String condition) {
        return db.queryForObject(
                "SELECT COUNT(*) FROM activity_outbox WHERE " + condition, Long.class);
    }

    @Test
    void legacyKoreanTimesAndOutfitUtcUseCorrectCutoffAndEventInstant() {
        var korean = new ActivityBackfill(db, outbox, "Asia/Seoul", "Asia/Seoul");
        db.update("UPDATE post SET created_at='2026-01-01 09:00:00' WHERE id=1");
        db.update("UPDATE outfit_post SET created_at='2026-01-01 00:00:00' WHERE id=1");
        db.update("UPDATE guild SET registered_at='2026-01-01 09:00:00' WHERE id=1");
        tx.executeWithoutResult(status -> korean.freeze(Instant.parse("2026-01-01T00:01:00Z")));
        for (ActivitySource source :
                List.of(ActivitySource.POST, ActivitySource.OUTFIT, ActivitySource.GUILD)) {
            tx.executeWithoutResult(status -> korean.step(source, 100));
        }
        assertEquals(3, count("baseline=true"));
        assertEquals(
                3L,
                db.queryForObject(
                        "SELECT COUNT(*) FROM activity_outbox WHERE occurred_at='2026-01-01 00:00:00'",
                        Long.class));
    }

    @Test
    void sourceAndOutboxRollbackTogetherAndSelfLikesAreExcluded() {
        assertThrows(
                IllegalStateException.class,
                () ->
                        tx.executeWithoutResult(
                                status -> {
                                    db.update("UPDATE post SET title='rolled back' WHERE id=1");
                                    outbox.created(ActivitySource.POST, 1, 1, 1);
                                    throw new IllegalStateException("rollback");
                                }));
        assertEquals(0, count("1=1"));
        assertEquals("post", db.queryForObject("SELECT title FROM post WHERE id=1", String.class));
        tx.executeWithoutResult(
                status -> outbox.changed(ActivitySource.POST_LIKE, 1, 1, 1, 1, true));
        assertEquals(0, count("1=1"));
    }

    @Test
    void deletingPreviouslyPublishedDraftStillReversesBlockedAndVisibleChildren() {
        db.update("UPDATE post SET first_published_at=created_at,is_draft=true WHERE id=1");
        db.update("UPDATE comment SET is_blocked=true WHERE id=1");
        tx.executeWithoutResult(
                status -> {
                    outbox.lockPost(1);
                    outbox.deletedRoot("POST", 1);
                    db.update("UPDATE post SET is_deleted=true WHERE id=1");
                    db.update("UPDATE comment SET post_id=NULL WHERE post_id=1");
                });
        assertEquals(5, count("active=false AND root_target_type='POST' AND root_target_id=1"));
    }

    @Test
    void firstPublicationAfterCutoffCannotBeBackfilledUsingOldDraftCreationTime() {
        db.update(
                "UPDATE post SET created_at='2026-01-01 00:00:00',first_published_at='2026-01-03 00:00:00' WHERE id=1");
        tx.executeWithoutResult(status -> backfill.freeze(Instant.parse("2026-01-02T00:00:00Z")));
        tx.executeWithoutResult(status -> backfill.step(ActivitySource.POST, 100));
        assertEquals(0, count("baseline=true AND target_type='POST'"));
    }

    @Test
    void backfillCoversAllSourcesResumesAndIsIdempotent() {
        Instant t0 = Instant.now().truncatedTo(ChronoUnit.MICROS);
        tx.executeWithoutResult(status -> backfill.freeze(t0));
        assertEquals(
                11,
                backfill.dryRun().stream()
                        .mapToLong(ActivityBackfill.Progress::remainingValid)
                        .sum());
        assertEquals(0, count("1=1"));
        assertThrows(
                IllegalStateException.class,
                () ->
                        tx.executeWithoutResult(
                                status -> {
                                    backfill.step(ActivitySource.COMMENT, 1);
                                    throw new IllegalStateException("crash");
                                }));
        assertEquals(0, count("1=1"));
        var restarted = new ActivityBackfill(db, outbox, "UTC", "UTC");
        for (int round = 0; round < 5; round++) {
            for (ActivitySource source : ActivitySource.values())
                tx.executeWithoutResult(status -> restarted.step(source, 1));
        }
        assertEquals(11, count("baseline=true"));
        assertTrue(restarted.dryRun().stream().allMatch(ActivityBackfill.Progress::complete));
        tx.executeWithoutResult(status -> backfill.freeze(t0));
        assertThrows(
                IllegalStateException.class,
                () -> tx.executeWithoutResult(status -> backfill.freeze(t0.minusSeconds(1))));
        assertEquals(0, count("actor_user_id=author_user_id AND activity_type='LIKE'"));
        assertEquals(0, count("target_type='POST' AND target_id IN (2,3,4)"));
    }

    @Test
    void parentDeleteCapturesChildrenBeforeDisconnectionAndStopsNewWrites() {
        tx.executeWithoutResult(
                status -> {
                    outbox.lockPost(1);
                    outbox.deletedRoot("POST", 1);
                    db.update("UPDATE post SET is_deleted=true WHERE id=1");
                    db.update("UPDATE comment SET post_id=NULL WHERE post_id=1");
                });
        assertEquals(5, count("active=false AND root_target_type='POST' AND root_target_id=1"));
        assertEquals(2, count("activity_type='COMMENT_CREATED'"));
        assertThrows(
                RuntimeException.class,
                () -> tx.executeWithoutResult(status -> outbox.lockPost(1)));
        tx.executeWithoutResult(
                status -> backfill.freeze(Instant.now().truncatedTo(ChronoUnit.MICROS)));
        for (ActivitySource source : ActivitySource.values())
            tx.executeWithoutResult(status -> backfill.step(source, 100));
        assertEquals(0, count("baseline=true AND root_target_type='POST'"));
    }

    @Test
    void deletingCommentKeepsReplyAndRevokesOnlyItsOwnLike() {
        tx.executeWithoutResult(
                status -> {
                    outbox.lockCommentRoot(1);
                    outbox.deletedComment(1);
                    db.update("UPDATE comment SET is_deleted=true,post_id=NULL WHERE id=1");
                });
        assertEquals(2, count("active=false"));
        assertEquals(0, count("target_type='COMMENT' AND target_id=2"));
        tx.executeWithoutResult(
                status -> backfill.freeze(Instant.now().truncatedTo(ChronoUnit.MICROS)));
        tx.executeWithoutResult(status -> backfill.step(ActivitySource.COMMENT, 100));
        assertEquals(1, count("baseline=true AND target_id=2"));
    }

    @Test
    void parentLockSerializesConcurrentEventsAndClaimsHaveExclusiveLeases() throws Exception {
        CountDownLatch locked = new CountDownLatch(1), release = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first =
                    executor.submit(
                            () ->
                                    tx.executeWithoutResult(
                                            status -> {
                                                outbox.lockPost(1);
                                                outbox.changed(
                                                        ActivitySource.POST_LIKE, 1, 2, 1, 1, true);
                                                locked.countDown();
                                                try {
                                                    assertTrue(release.await(10, TimeUnit.SECONDS));
                                                } catch (InterruptedException e) {
                                                    Thread.currentThread().interrupt();
                                                    throw new IllegalStateException(e);
                                                }
                                            }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            var second =
                    executor.submit(
                            () ->
                                    tx.executeWithoutResult(
                                            status -> {
                                                outbox.lockPost(1);
                                                outbox.changed(
                                                        ActivitySource.POST_LIKE,
                                                        1,
                                                        2,
                                                        1,
                                                        1,
                                                        false);
                                            }));
            release.countDown();
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
        }
        assertEquals(
                List.of(true, false),
                db.query(
                        "SELECT active FROM activity_outbox ORDER BY id",
                        (r, n) -> r.getBoolean(1)));
        var publisher = publisher(mock(KafkaTemplate.class));
        var one = tx.execute(status -> publisher.claim());
        var two = tx.execute(status -> publisher.claim());
        assertNotNull(one);
        assertNotNull(two);
        assertNotEquals(one.id(), two.id());
        assertNull(tx.execute(status -> publisher.claim()));
        db.update(
                "UPDATE activity_outbox SET claimed_until=DATE_SUB(UTC_TIMESTAMP(6),INTERVAL 1 SECOND) WHERE id=?",
                one.id());
        var reclaimed = tx.execute(status -> publisher.claim());
        assertEquals(one.event().eventId(), reclaimed.event().eventId());
        assertNotEquals(one.token(), reclaimed.token());
    }

    @Test
    @SuppressWarnings("unchecked")
    void brokerFailureRetainsRecordAndRetryKeepsEventIdentity() throws Exception {
        assertEquals(
                "IllegalStateException",
                ActivityOutboxPublisher.failureType(
                        new ExecutionException(
                                new IllegalStateException(
                                        "sensitive payload must not be logged"))));
        tx.executeWithoutResult(status -> outbox.created(ActivitySource.POST, 1, 1, 1));
        KafkaTemplate<Object, byte[]> kafka = mock(KafkaTemplate.class);
        when(kafka.send(eq(ActivityOutboxPublisher.TOPIC), any(), any(byte[].class)))
                .thenReturn(
                        CompletableFuture.failedFuture(
                                new IllegalStateException("broker offline")));
        var publisher = publisher(kafka);
        publisher.publish();
        assertEquals(1, count("published_at IS NULL AND attempts=1 AND claim_token IS NULL"));
        assertEquals(1, count("next_attempt_at>UTC_TIMESTAMP(6)"));
        db.update("UPDATE activity_outbox SET next_attempt_at=UTC_TIMESTAMP(6)");
        when(kafka.send(eq(ActivityOutboxPublisher.TOPIC), any(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        publisher.publish();
        assertEquals(1, count("published_at IS NOT NULL AND attempts=2"));
        var bytes = org.mockito.ArgumentCaptor.forClass(byte[].class);
        verify(kafka, times(2))
                .send(eq(ActivityOutboxPublisher.TOPIC), eq("POST:1:CREATE"), bytes.capture());
        assertArrayEquals(bytes.getAllValues().get(0), bytes.getAllValues().get(1));
        var json = mapper().readTree(bytes.getValue());
        assertEquals("POST_PUBLISHED", json.get("activityType").asText());
        assertTrue(json.get("sourceRevision").asLong() > 0);
        assertTrue(json.get("occurredAt").isTextual());
        assertFalse(json.has("xp"));
    }

    ActivityOutboxPublisher publisher(KafkaTemplate<Object, byte[]> kafka) {
        return new ActivityOutboxPublisher(db, tx, kafka, mapper());
    }

    ObjectMapper mapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
