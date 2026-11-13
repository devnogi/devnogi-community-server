package until.the.eternity.dcs.domain.seo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class PublicSitemapRepositoryTest {
    private JdbcTemplate jdbc;
    private PublicSitemapService service;
    private static final LocalDateTime MODIFIED = LocalDateTime.of(2026, 11, 13, 9, 15, 30);

    @BeforeEach
    void setup() {
        var ds =
                new DriverManagerDataSource(
                        "jdbc:h2:mem:sitemap" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
                        "sa",
                        "");
        jdbc = new JdbcTemplate(ds);
        // Deliberately no body, author, image, or viewer tables: the projection cannot hydrate
        // them.
        jdbc.execute(
                "CREATE TABLE post (id BIGINT PRIMARY KEY, updated_at TIMESTAMP,"
                        + " is_deleted BOOLEAN, is_blocked BOOLEAN, is_draft BOOLEAN)");
        jdbc.execute(
                "CREATE TABLE outfit_post (id BIGINT PRIMARY KEY, updated_at TIMESTAMP,"
                        + " is_deleted BOOLEAN)");
        service =
                new PublicSitemapService(
                        new PublicSitemapRepository(new NamedParameterJdbcTemplate(ds)));
    }

    @Test
    void postPagesExcludeHiddenRowsAndContinueAcrossIdGapsWithoutDuplicates() {
        post(1, false, false, false);
        post(2, true, false, false);
        post(3, false, true, false);
        post(4, false, false, true);
        post(8, false, false, false);
        post(11, false, false, false);

        var first = service.posts(null, "2");
        assertThat(first.items()).extracting(PublicSitemapPage.Entry::id).containsExactly(1L, 8L);
        assertThat(first.items().getFirst().updatedAt()).isEqualTo("2026-11-13T09:15:30+09:00");
        assertThat(first.hasNext()).isTrue();
        assertThat(first.nextCursor()).isEqualTo("8");

        var second = service.posts(first.nextCursor(), "2");
        assertThat(second.items()).extracting(PublicSitemapPage.Entry::id).containsExactly(11L);
        assertThat(second.hasNext()).isFalse();
        assertThat(second.nextCursor()).isNull();
        assertThat(service.posts("11", "2").items()).isEmpty();
    }

    @Test
    void outfitPagesExcludeDeletedRowsAndPreserveUtcModificationTime() {
        outfit(1, false);
        outfit(2, true);
        outfit(3, false);
        var first = service.outfits(null, "1");
        assertThat(first.items()).extracting(PublicSitemapPage.Entry::id).containsExactly(1L);
        assertThat(first.items().getFirst().updatedAt()).isEqualTo("2026-11-13T09:15:30Z");
        assertThat(first.hasNext()).isTrue();
        assertThat(first.nextCursor()).isEqualTo("1");
        var second = service.outfits("1", "1");
        assertThat(second.items()).extracting(PublicSitemapPage.Entry::id).containsExactly(3L);
        assertThat(second.hasNext()).isFalse();
        assertThat(second.nextCursor()).isNull();
    }

    @Test
    void deletionAndBlockingAreVisibleOnTheNextQueryAndMissingModificationIsNotInvented() {
        post(1, false, false, false);
        post(2, false, false, false);
        outfit(1, false);
        assertThat(service.posts(null, null).items()).hasSize(2);
        assertThat(service.outfits(null, null).items()).hasSize(1);
        jdbc.update("UPDATE post SET is_blocked=true WHERE id=1");
        jdbc.update("UPDATE post SET updated_at=NULL WHERE id=2");
        jdbc.update("UPDATE outfit_post SET is_deleted=true WHERE id=1");
        assertThat(service.posts(null, null).items())
                .containsExactly(new PublicSitemapPage.Entry(2, null));
        assertThat(service.outfits(null, null).items()).isEmpty();
    }

    @Test
    void defaultPageIsBoundedAndLookaheadDoesNotLeakIntoTheReturnedItems() {
        for (int i = 1; i <= 1001; i++) post(i, false, false, false);
        var first = service.posts(null, null);
        assertThat(first.items()).hasSize(1000);
        assertThat(first.nextCursor()).isEqualTo("1000");
        var last = service.posts(first.nextCursor(), null);
        assertThat(last.items()).extracting(PublicSitemapPage.Entry::id).containsExactly(1001L);
        assertThat(last.hasNext()).isFalse();
        assertThat(service.posts(Long.toString(Long.MAX_VALUE), null).items()).isEmpty();
    }

    private void post(long id, boolean deleted, boolean blocked, boolean draft) {
        jdbc.update("INSERT INTO post VALUES(?,?,?,?,?)", id, MODIFIED, deleted, blocked, draft);
    }

    private void outfit(long id, boolean deleted) {
        jdbc.update("INSERT INTO outfit_post VALUES(?,?,?)", id, MODIFIED, deleted);
    }
}
