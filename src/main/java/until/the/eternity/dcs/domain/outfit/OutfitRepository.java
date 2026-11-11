package until.the.eternity.dcs.domain.outfit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
@RequiredArgsConstructor
public class OutfitRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public record PostRow(
            long id,
            long userId,
            String content,
            boolean deleted,
            long likes,
            long comments,
            long shares,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    public record ImageRow(long id, long outfitId, String storedName, int displayOrder) {}

    private static final RowMapper<PostRow> POST =
            (r, n) ->
                    new PostRow(
                            r.getLong("id"),
                            r.getLong("user_id"),
                            r.getString("content"),
                            r.getBoolean("is_deleted"),
                            r.getLong("like_count"),
                            r.getLong("comment_count"),
                            r.getLong("share_count"),
                            r.getTimestamp("created_at").toLocalDateTime(),
                            r.getTimestamp("updated_at").toLocalDateTime());
    private static final RowMapper<ImageRow> IMAGE =
            (r, n) ->
                    new ImageRow(
                            r.getLong("id"),
                            r.getLong("outfit_id"),
                            r.getString("stored_name"),
                            r.getInt("display_order"));

    public PostRow post(long id, boolean lock) {
        List<PostRow> rows =
                jdbc.query(
                        "SELECT * FROM outfit_post WHERE id=:id" + (lock ? " FOR UPDATE" : ""),
                        Map.of("id", id),
                        POST);
        if (rows.isEmpty() || rows.getFirst().deleted())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "의장을 찾을 수 없습니다.");
        return rows.getFirst();
    }

    public long insert(String sql, MapSqlParameterSource params) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(sql, params, key, new String[] {"id"});
        return Objects.requireNonNull(key.getKey()).longValue();
    }

    public int update(String sql, Map<String, ?> params) {
        return jdbc.update(sql, params);
    }

    public int update(String sql, MapSqlParameterSource params) {
        return jdbc.update(sql, params);
    }

    public long count(String sql, Map<String, ?> params) {
        return Objects.requireNonNull(jdbc.queryForObject(sql, params, Long.class));
    }

    public List<ImageRow> images(long id) {
        return jdbc.query(
                "SELECT * FROM outfit_image WHERE outfit_id=:id ORDER BY display_order,id",
                Map.of("id", id),
                IMAGE);
    }

    public Map<Long, List<ImageRow>> images(List<Long> ids) {
        Map<Long, List<ImageRow>> result = new HashMap<>();
        jdbc.query(
                        "SELECT * FROM outfit_image WHERE outfit_id IN (:ids) ORDER BY display_order,id",
                        Map.of("ids", ids),
                        IMAGE)
                .forEach(v -> result.computeIfAbsent(v.outfitId(), k -> new ArrayList<>()).add(v));
        return result;
    }

    public Map<Long, OutfitDtos.Author> authors(List<Long> ids) {
        Map<Long, OutfitDtos.Author> result = new HashMap<>();
        jdbc.query(
                "SELECT id,nickname,profile_image_url,server_name,is_verified,activity_grade FROM user_summary WHERE id IN (:ids)",
                Map.of("ids", ids),
                (org.springframework.jdbc.core.RowCallbackHandler)
                        r -> {
                            long id = r.getLong("id");
                            result.put(
                                    id,
                                    new OutfitDtos.Author(
                                            id,
                                            r.getString("nickname"),
                                            r.getString("profile_image_url"),
                                            r.getString("server_name"),
                                            r.getBoolean("is_verified"),
                                            r.getBoolean("is_verified")
                                                    ? r.getString("activity_grade")
                                                    : null));
                        });
        return result;
    }

    public Map<Long, List<OutfitDtos.Item>> items(List<Long> ids) {
        Map<Long, List<OutfitDtos.Item>> result = new HashMap<>();
        jdbc.query(
                "SELECT * FROM outfit_item WHERE outfit_id IN (:ids) ORDER BY id",
                Map.of("ids", ids),
                (org.springframework.jdbc.core.RowCallbackHandler)
                        r ->
                                result.computeIfAbsent(
                                                r.getLong("outfit_id"), k -> new ArrayList<>())
                                        .add(
                                                new OutfitDtos.Item(
                                                        r.getString("slot"),
                                                        r.getString("input_type"),
                                                        r.getString("name"),
                                                        r.getString("top_category"),
                                                        r.getString("sub_category"))));
        return result;
    }

    public Map<Long, List<String>> tags(List<Long> ids) {
        Map<Long, List<String>> result = new HashMap<>();
        jdbc.query(
                "SELECT * FROM outfit_tag WHERE outfit_id IN (:ids) ORDER BY id",
                Map.of("ids", ids),
                (org.springframework.jdbc.core.RowCallbackHandler)
                        r ->
                                result.computeIfAbsent(
                                                r.getLong("outfit_id"), k -> new ArrayList<>())
                                        .add(r.getString("tag")));
        return result;
    }

    public List<PostRow> list(
            String sort,
            int size,
            int page,
            String cursor,
            List<OutfitDtos.Item> filters,
            List<String> tags,
            String motion,
            Long user,
            boolean saved) {
        MapSqlParameterSource p = new MapSqlParameterSource("limit", size + 1);
        StringBuilder sql =
                new StringBuilder("SELECT p.* FROM outfit_post p WHERE p.is_deleted=false");
        if (user != null) {
            p.addValue("user", user);
            sql.append(
                    saved
                            ? " AND EXISTS (SELECT 1 FROM outfit_bookmark b WHERE b.outfit_id=p.id AND b.user_id=:user)"
                            : " AND p.user_id=:user");
        }
        if ("POPULAR".equals(sort)) {
            sql.append(" AND p.created_at>=:since");
            p.addValue("since", LocalDateTime.now(java.time.Clock.systemUTC()).minusDays(7));
        }
        for (int i = 0; i < filters.size(); i++) {
            OutfitDtos.Item f = filters.get(i);
            String alias = "i" + i;
            sql.append(" AND EXISTS (SELECT 1 FROM outfit_item ")
                    .append(alias)
                    .append(" WHERE ")
                    .append(alias)
                    .append(".outfit_id=p.id AND ")
                    .append(itemCondition(alias, "f" + i, f, p))
                    .append(")");
        }
        List<OutfitDtos.Item> accessory =
                filters.stream()
                        .filter(f -> f.slot() != null && f.slot().startsWith("ACCESSORY_"))
                        .toList();
        if (accessory.size() == 2) {
            sql.append(
                            " AND EXISTS (SELECT 1 FROM outfit_item a JOIN outfit_item b ON a.outfit_id=b.outfit_id AND a.id<>b.id WHERE a.outfit_id=p.id AND ")
                    .append(itemCondition("a", "aa", accessory.get(0), p))
                    .append(" AND ")
                    .append(itemCondition("b", "ab", accessory.get(1), p))
                    .append(")");
        }
        for (int i = 0; i < tags.size(); i++) {
            sql.append(
                            " AND EXISTS (SELECT 1 FROM outfit_tag t WHERE t.outfit_id=p.id AND t.normalized_tag=:tag")
                    .append(i)
                    .append(")");
            p.addValue("tag" + i, OutfitValidation.normalized(tags.get(i)));
        }
        if (motion != null && !motion.isBlank()) {
            sql.append(
                    " AND EXISTS (SELECT 1 FROM outfit_item m WHERE m.outfit_id=p.id AND m.slot='MOTION' AND m.normalized_name=:motion)");
            p.addValue("motion", OutfitValidation.normalized(motion));
        }
        if (!"POPULAR".equals(sort) && cursor != null && !cursor.isBlank()) {
            try {
                String[] parts =
                        new String(
                                        Base64.getUrlDecoder().decode(cursor),
                                        java.nio.charset.StandardCharsets.UTF_8)
                                .split("\\|", -1);
                if (parts.length != 2) throw new IllegalArgumentException();
                p.addValue("cursorDate", LocalDateTime.parse(parts[0]))
                        .addValue("cursorId", Long.parseLong(parts[1]));
            } catch (IllegalArgumentException | java.time.DateTimeException e) {
                throw OutfitValidation.invalid("잘못된 페이지 커서입니다.");
            }
            sql.append(
                    " AND (p.created_at<:cursorDate OR (p.created_at=:cursorDate AND p.id<:cursorId))");
        }
        sql.append(" ORDER BY ")
                .append("POPULAR".equals(sort) ? "p.like_count DESC," : "")
                .append("p.created_at DESC,p.id DESC LIMIT :limit");
        if ("POPULAR".equals(sort)) {
            sql.append(" OFFSET :offset");
            p.addValue("offset", (long) page * size);
        }
        return jdbc.query(sql.toString(), p, POST);
    }

    private String itemCondition(
            String alias, String key, OutfitDtos.Item item, MapSqlParameterSource params) {
        params.addValue(
                key + "name",
                item.inputType().equals("CUSTOM")
                        ? OutfitValidation.normalized(item.name())
                        : item.name());
        String value =
                alias
                        + (item.inputType().equals("CUSTOM") ? ".normalized_name=:" : ".name=:")
                        + key
                        + "name";
        params.addValue(key + "type", item.inputType());
        value += " AND " + alias + ".input_type=:" + key + "type";
        if (item.slot() != null) {
            if (item.slot().startsWith("ACCESSORY_"))
                value += " AND " + alias + ".slot IN ('ACCESSORY_1','ACCESSORY_2')";
            else {
                value += " AND " + alias + ".slot=:" + key + "slot";
                params.addValue(key + "slot", item.slot());
            }
        }
        if (item.inputType().equals("CATALOG")) {
            value +=
                    " AND "
                            + alias
                            + ".top_category=:"
                            + key
                            + "top AND "
                            + alias
                            + ".sub_category=:"
                            + key
                            + "sub";
            params.addValue(key + "top", item.topCategory())
                    .addValue(key + "sub", item.subCategory());
        }
        return value;
    }

    public Set<Long> reactionIds(String table, long user, List<Long> ids) {
        if (!Set.of("outfit_like", "outfit_bookmark").contains(table))
            throw new IllegalArgumentException();
        return new HashSet<>(
                jdbc.queryForList(
                        "SELECT r.outfit_id FROM "
                                + table
                                + " r JOIN outfit_post p ON p.id=r.outfit_id WHERE r.user_id=:user AND r.outfit_id IN (:ids) AND p.is_deleted=false",
                        Map.of("user", user, "ids", ids),
                        Long.class));
    }

    public record CommentRow(
            long id,
            long outfitId,
            long userId,
            String content,
            Long parentId,
            boolean deleted,
            long replies,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    private static final RowMapper<CommentRow> COMMENT =
            (r, n) ->
                    new CommentRow(
                            r.getLong("id"),
                            r.getLong("outfit_id"),
                            r.getLong("user_id"),
                            r.getString("content"),
                            r.getObject("parent_id") == null ? null : r.getLong("parent_id"),
                            r.getBoolean("is_deleted"),
                            r.getLong("replies"),
                            r.getTimestamp("created_at").toLocalDateTime(),
                            r.getTimestamp("updated_at").toLocalDateTime());

    public CommentRow comment(long id, long post) {
        List<CommentRow> rows =
                jdbc.query(
                        "SELECT c.*,0 AS replies FROM outfit_comment c WHERE c.id=:id AND c.outfit_id=:post",
                        Map.of("id", id, "post", post),
                        COMMENT);
        if (rows.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");
        return rows.getFirst();
    }

    public List<CommentRow> comments(long post, Long parent, int page, int size) {
        return jdbc.query(
                "SELECT c.*,(SELECT COUNT(*) FROM outfit_comment r WHERE r.parent_id=c.id AND r.is_deleted=false) AS replies FROM outfit_comment c WHERE c.outfit_id=:post AND "
                        + (parent == null ? "c.parent_id IS NULL" : "c.parent_id=:parent")
                        + " AND (c.is_deleted=false OR EXISTS(SELECT 1 FROM outfit_comment r WHERE r.parent_id=c.id AND r.is_deleted=false)) ORDER BY c.created_at,c.id LIMIT :limit OFFSET :offset",
                new MapSqlParameterSource("post", post)
                        .addValue("parent", parent)
                        .addValue("limit", size + 1)
                        .addValue("offset", (long) page * size),
                COMMENT);
    }

    public List<String> motions(String query) {
        String term =
                OutfitValidation.normalized(query)
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_");
        return jdbc.queryForList(
                "SELECT MIN(i.name) FROM outfit_item i JOIN outfit_post p ON p.id=i.outfit_id WHERE p.is_deleted=false AND i.slot='MOTION' AND i.input_type='CUSTOM' AND i.normalized_name LIKE :query ESCAPE '!' GROUP BY i.normalized_name ORDER BY i.normalized_name LIMIT 20",
                Map.of("query", "%" + term + "%"),
                String.class);
    }
}
