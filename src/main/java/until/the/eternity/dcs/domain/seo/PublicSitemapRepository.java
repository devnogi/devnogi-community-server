package until.the.eternity.dcs.domain.seo;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PublicSitemapRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public List<PublicSitemapPage.Entry> posts(long afterId, int limit) {
        return jdbc.query(
                "SELECT id, updated_at FROM post WHERE id > :afterId"
                        + " AND is_deleted = false AND is_blocked = false AND is_draft = false"
                        + " ORDER BY id ASC LIMIT :limit",
                Map.of("afterId", afterId, "limit", limit),
                entry(ZoneId.of("Asia/Seoul")));
    }

    public List<PublicSitemapPage.Entry> outfits(long afterId, int limit) {
        return jdbc.query(
                "SELECT id, updated_at FROM outfit_post WHERE id > :afterId"
                        + " AND is_deleted = false ORDER BY id ASC LIMIT :limit",
                Map.of("afterId", afterId, "limit", limit),
                entry(ZoneOffset.UTC));
    }

    private RowMapper<PublicSitemapPage.Entry> entry(ZoneId sourceZone) {
        return (row, index) -> {
            Timestamp updatedAt = row.getTimestamp("updated_at");
            return new PublicSitemapPage.Entry(
                    row.getLong("id"),
                    updatedAt == null
                            ? null
                            : updatedAt
                                    .toLocalDateTime()
                                    .atZone(sourceZone)
                                    .toOffsetDateTime()
                                    .toString());
        };
    }
}
