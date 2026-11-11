package until.the.eternity.dcs.domain.guild;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import until.the.eternity.dcs.domain.experience.ActivityOutbox;
import until.the.eternity.dcs.domain.experience.ActivitySource;

@Service
@RequiredArgsConstructor
public class GuildContent {
    final JdbcTemplate db;
    private final ActivityOutbox activityOutbox;
    final GuildAccess access;
    static final String POSTS =
            "SELECT p.id,p.guild_id AS guildId,p.author_id AS authorId,u.nickname AS authorNickname,COALESCE(u.is_verified,0) AS authorVerified,CASE WHEN u.is_verified=1 THEN u.activity_grade ELSE NULL END AS activityGrade,p.title,p.body,p.visibility,(p.photo_key IS NOT NULL) AS hasPhoto,p.created_at AS createdAt,p.updated_at AS updatedAt FROM guild_post p LEFT JOIN user_summary u ON u.id=p.author_id JOIN guild g ON g.id=p.guild_id WHERE g.is_deleted=0 AND p.is_deleted=0 AND p.guild_id=? AND (p.visibility='PUBLIC' OR EXISTS(SELECT 1 FROM guild_member m WHERE m.guild_id=g.id AND m.user_id=? AND m.left_at IS NULL)) ";
    static final String RECRUITS =
            "SELECT r.id,r.guild_id AS guildId,r.author_id AS authorId,u.nickname AS authorNickname,COALESCE(u.is_verified,0) AS authorVerified,CASE WHEN u.is_verified=1 THEN u.activity_grade ELSE NULL END AS activityGrade,r.title,r.body,r.requirements,r.status,r.created_at AS createdAt,r.updated_at AS updatedAt FROM guild_recruitment_post r LEFT JOIN user_summary u ON u.id=r.author_id JOIN guild g ON g.id=r.guild_id WHERE g.is_deleted=0 AND r.is_deleted=0 AND r.guild_id=? ";
    static final String GUILDS =
            "SELECT g.id,g.name,g.server,g.master_id AS masterId,g.register_id AS registerId,p.tagline,p.description,p.theme,p.tags,p.activity_time AS activityTime,(p.banner_key IS NOT NULL) AS hasBanner,(p.emblem_key IS NOT NULL) AS hasEmblem, EXISTS(SELECT 1 FROM guild_recruitment_post r WHERE r.guild_id=g.id AND r.status='OPEN' AND r.is_deleted=0) AS recruiting FROM guild g LEFT JOIN guild_profile p ON p.guild_id=g.id WHERE g.is_deleted=0 ";

    static ResponseStatusException error(HttpStatus status, String text) {
        return new ResponseStatusException(status, text);
    }

    static String field(String value, int max, boolean required) {
        String s = value == null ? "" : value.strip();
        if (s.length() > max || (required && s.isEmpty()))
            throw error(HttpStatus.BAD_REQUEST, "입력 길이를 확인해 주세요.");
        return s;
    }

    Map<String, Object> one(String sql, Object... args) {
        var rows = db.queryForList(sql, args);
        if (rows.isEmpty()) throw error(HttpStatus.NOT_FOUND, "찾을 수 없습니다.");
        return rows.getFirst();
    }

    public List<Map<String, Object>> list(
            String server, String name, boolean recruiting, int page) {
        return db.queryForList(
                GUILDS
                        + " AND (?='' OR g.server=?) AND LOCATE(?,g.name)>0 HAVING (?=false OR recruiting=1) ORDER BY g.id DESC LIMIT 30 OFFSET ?",
                server,
                server,
                name,
                recruiting,
                offset(page));
    }

    static long offset(int page) {
        if (page < 0) throw error(HttpStatus.BAD_REQUEST, "페이지 번호를 확인해 주세요.");
        return 30L * page;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> home(int g) {
        var row = new LinkedHashMap<>(one(GUILDS + " AND g.id=?", g));
        Long viewer = access.viewer();
        String role = viewer == null ? null : access.role(g, viewer);
        row.put("viewerRole", role);
        row.put(
                "canEdit",
                role != null
                        && (Objects.equals(viewer, ((Number) row.get("registerId")).longValue())
                                || operator(role)));
        row.put("posts", db.queryForList(POSTS + " ORDER BY p.id DESC LIMIT 5", g, viewer));
        row.put(
                "recruitments",
                db.queryForList(RECRUITS + " AND r.status='OPEN' ORDER BY r.id DESC LIMIT 5", g));
        return row;
    }

    static boolean operator(String role) {
        return "MASTER".equals(role) || "DEPUTY".equals(role);
    }

    long officer(int g) {
        long u = access.user();
        if (!operator(access.role(g, u))) throw error(HttpStatus.FORBIDDEN, "길드 운영진 권한이 필요합니다.");
        return u;
    }

    long editor(int g) {
        long u = access.user();
        String role = access.role(g, u);
        if (role == null
                || (!operator(role)
                        && ((Number)
                                                one("SELECT register_id FROM guild WHERE id=?", g)
                                                        .get("register_id"))
                                        .longValue()
                                != u)) throw error(HttpStatus.FORBIDDEN, "홈 편집 권한이 필요합니다.");
        return u;
    }

    public record Profile(
            String tagline, String description, String theme, String tags, String activityTime) {}

    @Transactional
    public void profile(int g, Profile p) {
        access.lockGuild(g);
        long u = editor(g);
        if (p.theme() == null || !List.of("FOREST", "SKY", "ROSE", "GOLD").contains(p.theme()))
            throw error(HttpStatus.BAD_REQUEST, "테마를 확인해 주세요.");
        db.update(
                "UPDATE guild_profile SET tagline=?,description=?,theme=?,tags=?,activity_time=?,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE guild_id=?",
                field(p.tagline(), 120, false),
                field(p.description(), 4000, false),
                p.theme(),
                field(p.tags(), 200, false),
                field(p.activityTime(), 120, false),
                u,
                g);
    }

    public record Writing(String title, String body, String visibility, String requirements) {}

    public List<Map<String, Object>> writings(int g, boolean post, int page) {
        one("SELECT id FROM guild WHERE id=? AND is_deleted=0", g);
        return post
                ? db.queryForList(
                        POSTS + " ORDER BY p.id DESC LIMIT 30 OFFSET ?",
                        g,
                        access.viewer(),
                        offset(page))
                : db.queryForList(
                        RECRUITS + " ORDER BY r.id DESC LIMIT 30 OFFSET ?", g, offset(page));
    }

    public Map<String, Object> writing(int g, long id, boolean post) {
        return post
                ? one(POSTS + " AND p.id=?", g, access.viewer(), id)
                : one(RECRUITS + " AND r.id=?", g, id);
    }

    long author(int g, Long id, boolean post) {
        long u = access.user();
        if (post) {
            if (access.role(g, u) == null) throw error(HttpStatus.FORBIDDEN, "길드원 권한이 필요합니다.");
            if (id != null && ((Number) writing(g, id, true).get("authorId")).longValue() != u)
                throw error(HttpStatus.FORBIDDEN, "작성자 권한이 필요합니다.");
        } else {
            officer(g);
            if (id != null) writing(g, id, false);
        }
        return u;
    }

    @Transactional
    public Map<String, Long> save(int g, Long id, boolean post, Writing w) {
        access.lockGuild(g);
        long u = author(g, id, post);
        String title = field(w.title(), 150, true), body = field(w.body(), 10000, true);
        if (post) {
            String v = w.visibility() == null ? "MEMBERS" : w.visibility();
            if (!List.of("PUBLIC", "MEMBERS").contains(v))
                throw error(HttpStatus.BAD_REQUEST, "공개 범위를 확인해 주세요.");
            if (id == null)
                db.update(
                        "INSERT INTO guild_post(guild_id,author_id,title,body,visibility) VALUES(?,?,?,?,?)",
                        g,
                        u,
                        title,
                        body,
                        v);
            else
                db.update(
                        "UPDATE guild_post SET title=?,body=?,visibility=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                        title,
                        body,
                        v,
                        id);
        } else {
            String req = field(w.requirements(), 1000, false);
            if (id == null)
                db.update(
                        "INSERT INTO guild_recruitment_post(guild_id,author_id,title,body,requirements,status) VALUES(?,?,?,?,?,'OPEN')",
                        g,
                        u,
                        title,
                        body,
                        req);
            else
                db.update(
                        "UPDATE guild_recruitment_post SET title=?,body=?,requirements=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                        title,
                        body,
                        req,
                        id);
        }
        long savedId =
                id == null
                        ? Objects.requireNonNull(
                                db.queryForObject("SELECT LAST_INSERT_ID()", Long.class))
                        : id;
        if (id == null)
            activityOutbox.created(
                    post ? ActivitySource.GUILD_POST : ActivitySource.GUILD_RECRUITMENT,
                    savedId,
                    u,
                    savedId);
        return Map.of("id", savedId);
    }

    @Transactional
    public void remove(int g, long id, boolean post, boolean delete) {
        access.lockGuild(g);
        author(g, id, post);
        if (post || delete)
            activityOutbox.deletedRoot(post ? "GUILD_POST" : "GUILD_RECRUITMENT", id);
        if (post) db.update("UPDATE guild_post SET is_deleted=1 WHERE id=?", id);
        else {
            db.update(
                    "UPDATE guild_recruitment_post SET status='CLOSED',closed_at=COALESCE(closed_at,CURRENT_TIMESTAMP),is_deleted=? WHERE id=?",
                    delete,
                    id);
            db.update(
                    "UPDATE guild_join_application SET status='CANCELLED',reviewed_at=CURRENT_TIMESTAMP WHERE recruitment_id=? AND status='PENDING'",
                    id);
        }
    }
}
