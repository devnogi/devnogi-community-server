package until.the.eternity.dcs.domain.outfit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import until.the.eternity.dcs.common.infrastructure.MinioService;
import until.the.eternity.dcs.domain.outfit.catalog.OutfitCatalogService;

class OutfitServiceTest {
    protected OutfitService service;
    protected OutfitRepository repository;
    protected MinioService storage;
    protected OutfitCatalogService catalog;
    protected JdbcTemplate jdbc;

    protected DataSource dataSource() {
        DriverManagerDataSource ds =
                new DriverManagerDataSource(
                        "jdbc:h2:mem:outfit" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
                        "sa",
                        "");
        new ResourceDatabasePopulator(
                        new ClassPathResource(
                                "db/migration/V20261107000001__create_outfit_snap.sql"))
                .execute(ds);
        new JdbcTemplate(ds)
                .execute(
                        "CREATE TABLE user_summary(id BIGINT PRIMARY KEY,nickname VARCHAR(50),profile_image_url VARCHAR(255),server_name VARCHAR(50),is_verified BOOLEAN)");
        return ds;
    }

    @BeforeEach
    void setup() {
        DataSource ds = dataSource();
        jdbc = new JdbcTemplate(ds);
        jdbc.update("DELETE FROM outfit_comment WHERE parent_id IS NOT NULL");
        for (String table :
                List.of(
                        "outfit_share",
                        "outfit_like",
                        "outfit_bookmark",
                        "outfit_comment",
                        "outfit_image",
                        "outfit_item",
                        "outfit_tag",
                        "outfit_post")) jdbc.update("DELETE FROM " + table);
        jdbc.update("DELETE FROM user_summary WHERE id IN (90001,90002)");
        jdbc.update(
                "INSERT INTO user_summary(id,nickname,is_verified) VALUES(90001,'의장테스트1',false),(90002,'의장테스트2',false)");
        repository = new OutfitRepository(new NamedParameterJdbcTemplate(ds));
        storage = mock(MinioService.class);
        catalog = mock(OutfitCatalogService.class);
        when(storage.uploadFile(any())).thenAnswer(invocation -> UUID.randomUUID() + ".png");
        when(storage.getFileUrl(anyString()))
                .thenAnswer(invocation -> "https://storage.example/" + invocation.getArgument(0));
        service =
                new OutfitService(
                        repository,
                        storage,
                        catalog,
                        new TransactionTemplate(new DataSourceTransactionManager(ds)));
        login(90001);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    protected static void login(long id) {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(id, null, List.of()));
    }

    protected MockMultipartFile photo() {
        return new MockMultipartFile(
                "media",
                "photo.png",
                "image/png",
                new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 0, 0, 0, 0, 0});
    }

    protected List<OutfitDtos.Item> required() {
        return List.of(
                new OutfitDtos.Item("CLOTHING", "CATALOG", "테스트 의상", "갑옷 장비", "천옷"),
                new OutfitDtos.Item("HANDS", "CATALOG", "테스트 장갑", "방어 장비", "장갑"),
                new OutfitDtos.Item("FEET", "CATALOG", "테스트 신발", "방어 장비", "신발"));
    }

    protected OutfitDtos.Outfit create() {
        return service.write(
                null,
                new OutfitDtos.Write(
                        "착용샷",
                        List.of("#겨울"),
                        required(),
                        List.of(new OutfitDtos.MediaOrder(null, 0))),
                List.of(photo()));
    }

    @Test
    void requiredSlotsAndCustomMotionOnly() {
        var order = List.of(new OutfitDtos.MediaOrder(null, 0));
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.write(
                                null,
                                new OutfitDtos.Write(
                                        "", List.of(), required().subList(0, 2), order),
                                List.of(photo())));
        var invalid = new ArrayList<>(required());
        invalid.add(new OutfitDtos.Item("HEAD", "CUSTOM", "직접 입력", null, null));
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.write(
                                null,
                                new OutfitDtos.Write("", List.of(), invalid, order),
                                List.of(photo())));
        var valid = new ArrayList<>(required());
        valid.add(new OutfitDtos.Item("MOTION", "CUSTOM", "  의상 전용 모션  ", null, null));
        var post =
                service.write(
                        null,
                        new OutfitDtos.Write("", List.of("#겨울", "겨울"), valid, order),
                        List.of(photo()));
        assertEquals(1, post.tags().size());
        assertEquals("의상 전용 모션", post.items().getLast().name());
        assertEquals(List.of("의상 전용 모션"), service.motions("전용"));
        assertEquals(
                1,
                service.list("LATEST", 24, 0, null, List.of(), List.of(), "의상 전용 모션", null)
                        .content()
                        .size());
        verify(catalog).validateItems(argThat(items -> items.size() == 3));
    }

    @Test
    void anonymousReadButMutationsAndMineRequireLogin() {
        var post = create();
        SecurityContextHolder.clearContext();
        assertEquals(post.id(), service.detail(post.id()).id());
        assertEquals(
                401,
                assertThrows(
                                ResponseStatusException.class,
                                () -> service.reaction(post.id(), true, true))
                        .getStatusCode()
                        .value());
        assertThrows(
                ResponseStatusException.class,
                () -> service.list("LATEST", 24, 0, null, List.of(), List.of(), null, "mine"));
        assertThrows(
                ResponseStatusException.class,
                () -> service.comment(post.id(), null, new OutfitDtos.CommentWrite("hi", null)));
    }

    @Test
    void ownershipAndRetainedImagesAreEnforced() {
        var first = create();
        var second = create();
        login(90002);
        assertEquals(
                403,
                assertThrows(ResponseStatusException.class, () -> service.delete(first.id()))
                        .getStatusCode()
                        .value());
        login(90001);
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.write(
                                first.id(),
                                new OutfitDtos.Write(
                                        "",
                                        List.of(),
                                        required(),
                                        List.of(
                                                new OutfitDtos.MediaOrder(
                                                        second.images().getFirst().id(), null))),
                                List.of()));
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.write(
                                first.id(),
                                new OutfitDtos.Write(
                                        "",
                                        List.of(),
                                        required(),
                                        List.of(
                                                new OutfitDtos.MediaOrder(
                                                        first.images().getFirst().id(), null),
                                                new OutfitDtos.MediaOrder(
                                                        first.images().getFirst().id(), null))),
                                List.of()));
    }

    @Test
    void imageReplacementAndDeletionCleanFilesAfterCommit() {
        var post = create();
        String old = repository.images(post.id()).getFirst().storedName();
        clearInvocations(catalog);
        var updated =
                service.write(
                        post.id(),
                        new OutfitDtos.Write(
                                "changed",
                                List.of(),
                                required(),
                                List.of(new OutfitDtos.MediaOrder(null, 0))),
                        List.of(photo()));
        verify(storage).deleteFile(old);
        verify(catalog).validateItems(List.of());
        assertEquals(1, updated.images().size());
        service.delete(post.id());
        assertThrows(ResponseStatusException.class, () -> service.detail(post.id()));
        assertThrows(ResponseStatusException.class, () -> service.reaction(post.id(), true, true));
        assertTrue(
                service.list("LATEST", 24, 0, null, List.of(), List.of(), null, null)
                        .content()
                        .isEmpty());
    }

    @Test
    void catalogFailureDoesNotUploadOrStore() {
        doThrow(
                        new ResponseStatusException(
                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE))
                .when(catalog)
                .validateItems(any());
        assertThrows(ResponseStatusException.class, this::create);
        verify(storage, never()).uploadFile(any());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM outfit_post", Integer.class));
    }

    @Test
    void partialUploadFailureCleansUploadedObjects() {
        when(storage.uploadFile(any()))
                .thenReturn("first.png")
                .thenThrow(new IllegalStateException("storage failure"));
        assertThrows(
                IllegalStateException.class,
                () ->
                        service.write(
                                null,
                                new OutfitDtos.Write(
                                        "",
                                        List.of(),
                                        required(),
                                        List.of(
                                                new OutfitDtos.MediaOrder(null, 0),
                                                new OutfitDtos.MediaOrder(null, 1))),
                                List.of(photo(), photo())));
        verify(storage).deleteFile("first.png");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM outfit_post", Integer.class));
    }

    @Test
    void likesBookmarksSharesAreIdempotentAndPrivate() {
        var post = create();
        service.reaction(post.id(), true, true);
        service.reaction(post.id(), true, true);
        assertEquals(1, service.detail(post.id()).likeCount());
        service.reaction(post.id(), false, true);
        assertEquals(
                1,
                service.list("LATEST", 24, 0, null, List.of(), List.of(), null, "saved")
                        .content()
                        .size());
        login(90002);
        assertFalse(service.states(List.of(post.id())).getFirst().bookmarked());
        assertTrue(
                service.list("LATEST", 24, 0, null, List.of(), List.of(), null, "saved")
                        .content()
                        .isEmpty());
        var share = new OutfitDtos.ShareWrite(UUID.randomUUID().toString(), "CLIPBOARD");
        assertEquals(1, service.share(post.id(), share).shareCount());
        assertEquals(1, service.share(post.id(), share).shareCount());
        login(90001);
        service.reaction(post.id(), true, false);
        service.reaction(post.id(), true, false);
        assertEquals(0, service.detail(post.id()).likeCount());
    }

    @Test
    void commentParentsOwnershipAndDeletedPlaceholders() {
        var first = create();
        var second = create();
        var root = service.comment(first.id(), null, new OutfitDtos.CommentWrite("root", null));
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.comment(
                                second.id(),
                                null,
                                new OutfitDtos.CommentWrite("invalid", root.id())));
        var reply =
                service.comment(first.id(), null, new OutfitDtos.CommentWrite("reply", root.id()));
        assertThrows(
                ResponseStatusException.class,
                () ->
                        service.comment(
                                first.id(),
                                null,
                                new OutfitDtos.CommentWrite("nested", reply.id())));
        login(90002);
        assertThrows(
                ResponseStatusException.class, () -> service.deleteComment(first.id(), root.id()));
        login(90001);
        service.deleteComment(first.id(), root.id());
        service.deleteComment(first.id(), root.id());
        assertEquals(1, service.detail(first.id()).commentCount());
        var comments = service.comments(first.id(), null, 0, 20);
        assertTrue(comments.content().getFirst().deleted());
        assertEquals("", comments.content().getFirst().content());
        assertEquals(1, comments.content().getFirst().replyCount());
    }

    @Test
    void combinedFiltersAccessoryMultiplicityAndCursor() {
        var items = new ArrayList<>(required());
        var accessory = new OutfitDtos.Item("ACCESSORY_1", "CATALOG", "반지", "액세서리", "액세서리");
        items.add(accessory);
        var one =
                service.write(
                        null,
                        new OutfitDtos.Write(
                                "one",
                                List.of("겨울"),
                                items,
                                List.of(new OutfitDtos.MediaOrder(null, 0))),
                        List.of(photo()));
        var other = new OutfitDtos.Item("ACCESSORY_2", "CATALOG", "반지", "액세서리", "액세서리");
        items.add(other);
        var two =
                service.write(
                        null,
                        new OutfitDtos.Write(
                                "two",
                                List.of("겨울"),
                                items,
                                List.of(new OutfitDtos.MediaOrder(null, 0))),
                        List.of(photo()));
        var filtered =
                service.list(
                        "LATEST",
                        24,
                        0,
                        null,
                        List.of(accessory, other),
                        List.of("겨울"),
                        null,
                        null);
        assertEquals(
                List.of(two.id()), filtered.content().stream().map(OutfitDtos.Outfit::id).toList());
        var page = service.list("LATEST", 1, 0, null, List.of(), List.of(), null, null);
        assertTrue(page.hasNext());
        assertEquals(
                one.id(),
                service.list("LATEST", 1, 0, page.nextCursor(), List.of(), List.of(), null, null)
                        .content()
                        .getFirst()
                        .id());
        assertThrows(
                ResponseStatusException.class,
                () -> service.list("LATEST", 24, 0, "bad", List.of(), List.of(), null, null));
    }

    @Test
    void concurrentLikeRequestsCountOnce() throws Exception {
        var post = create();
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(4)) {
            List<java.util.concurrent.Callable<Void>> calls = new ArrayList<>();
            for (int i = 0; i < 8; i++)
                calls.add(
                        () -> {
                            login(90002);
                            try {
                                service.reaction(post.id(), true, true);
                            } finally {
                                SecurityContextHolder.clearContext();
                            }
                            return null;
                        });
            for (var future : executor.invokeAll(calls)) future.get();
        }
        assertEquals(1, service.detail(post.id()).likeCount());
    }

    @Test
    void malformedFieldsAndDateCursorAreClientErrors() {
        var order = List.of(new OutfitDtos.MediaOrder(null, 0));
        var items = new ArrayList<>(required());
        items.add(new OutfitDtos.Item(null, "CATALOG", "bad", "bad", "bad"));
        assertEquals(
                400,
                assertThrows(
                                ResponseStatusException.class,
                                () ->
                                        service.write(
                                                null,
                                                new OutfitDtos.Write("", List.of(), items, order),
                                                List.of(photo())))
                        .getStatusCode()
                        .value());
        var post = create();
        assertEquals(
                400,
                assertThrows(
                                ResponseStatusException.class,
                                () ->
                                        service.share(
                                                post.id(),
                                                new OutfitDtos.ShareWrite(
                                                        UUID.randomUUID().toString(), null)))
                        .getStatusCode()
                        .value());
        String cursor =
                Base64.getUrlEncoder()
                        .encodeToString(
                                "not-a-date|1".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(
                400,
                assertThrows(
                                ResponseStatusException.class,
                                () ->
                                        service.list(
                                                "LATEST", 24, 0, cursor, List.of(), List.of(), null,
                                                null))
                        .getStatusCode()
                        .value());
    }

    @Test
    void databaseRollbackCleansNewFilesAndLeavesNoPost() {
        var broken = spy(repository);
        doThrow(new IllegalStateException("database failure"))
                .when(broken)
                .insert(startsWith("INSERT INTO outfit_item"), any());
        var transactional =
                new OutfitService(
                        broken,
                        storage,
                        catalog,
                        new TransactionTemplate(
                                new DataSourceTransactionManager(
                                        Objects.requireNonNull(jdbc.getDataSource()))));
        when(storage.uploadFile(any())).thenReturn("rollback.png");
        assertThrows(
                IllegalStateException.class,
                () ->
                        transactional.write(
                                null,
                                new OutfitDtos.Write(
                                        "",
                                        List.of(),
                                        required(),
                                        List.of(new OutfitDtos.MediaOrder(null, 0))),
                                List.of(photo())));
        verify(storage).deleteFile("rollback.png");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM outfit_post", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM outfit_image", Integer.class));
    }

    @Test
    void responseTimesHaveUtcOffset() throws Exception {
        var post = create();
        var mapper =
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .findAndRegisterModules()
                        .disable(
                                com.fasterxml.jackson.databind.SerializationFeature
                                        .WRITE_DATES_AS_TIMESTAMPS);
        var tree = mapper.readTree(mapper.writeValueAsString(post));
        assertTrue(tree.get("createdAt").asText().endsWith("Z"));
        assertTrue(tree.get("updatedAt").asText().endsWith("Z"));
    }

    @Test
    void deletionAndReactionRaceLeavesConsistentCounters() throws Exception {
        var post = create();
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var start = new java.util.concurrent.CountDownLatch(1);
            var deletion =
                    executor.submit(
                            () -> {
                                login(90001);
                                try {
                                    start.await();
                                    service.delete(post.id());
                                } finally {
                                    SecurityContextHolder.clearContext();
                                }
                                return null;
                            });
            var reaction =
                    executor.submit(
                            () -> {
                                login(90002);
                                try {
                                    start.await();
                                    try {
                                        service.reaction(post.id(), true, true);
                                    } catch (ResponseStatusException error) {
                                        assertEquals(404, error.getStatusCode().value());
                                    }
                                } finally {
                                    SecurityContextHolder.clearContext();
                                }
                                return null;
                            });
            start.countDown();
            deletion.get();
            reaction.get();
        }
        assertEquals(
                404,
                assertThrows(ResponseStatusException.class, () -> service.detail(post.id()))
                        .getStatusCode()
                        .value());
        assertEquals(
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM outfit_like WHERE outfit_id=?",
                        Long.class,
                        post.id()),
                jdbc.queryForObject(
                        "SELECT like_count FROM outfit_post WHERE id=?", Long.class, post.id()));
    }
}
