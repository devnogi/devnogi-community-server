package until.the.eternity.dcs.domain.guild;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GuildRegistry {
    private final JdbcTemplate db;
    private final GuildAccess access;
    private static final String REGISTRATIONS =
            """
        SELECT r.id,r.name,r.description,r.server,r.character_name AS characterName,r.applicant_id AS applicantId,
        r.status,r.guild_id AS guildId,r.created_at AS createdAt,v.reason,v.reviewer_id AS reviewerId,v.reviewed_at AS reviewedAt
        FROM guild_registration_request r LEFT JOIN guild_registration_review v ON v.request_id=r.id
        """;

    public List<Map<String, Object>> mine() {
        return db.queryForList(
                REGISTRATIONS + " WHERE r.applicant_id=? ORDER BY r.id DESC", access.user());
    }

    public List<Map<String, Object>> queue() {
        access.requireAdmin();
        return db.queryForList(REGISTRATIONS + " ORDER BY (r.status='PENDING') DESC,r.id DESC");
    }

    @Transactional
    public void submit(String name, String description) {
        long user = access.user();
        access.lockUser(user);
        ensureFree(user);
        var identity = access.verified(user);
        name = bounded(name, 50, true);
        description = bounded(description, 2000, false);
        ensureAvailable(identity.server(), name);
        db.update(
                "INSERT INTO guild_registration_request(name,description,server,character_name,applicant_id) VALUES(?,?,?,?,?)",
                name,
                description,
                identity.server(),
                identity.characterName(),
                user);
    }

    @Transactional
    public void cancel(long id) {
        if (db.update(
                        "UPDATE guild_registration_request SET status='CANCELLED' WHERE id=? AND applicant_id=? AND status='PENDING'",
                        id,
                        access.user())
                != 1) throw conflict("취소 가능한 신청을 찾을 수 없습니다.");
    }

    @Transactional
    public void review(long id, String decision, String reason) {
        access.requireAdmin();
        if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision)) throw invalid();
        reason = bounded(reason, 2000, "REJECTED".equals(decision));
        var rows =
                db.queryForList(
                        "SELECT * FROM guild_registration_request WHERE id=? FOR UPDATE", id);
        if (rows.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신청을 찾을 수 없습니다.");
        var row = rows.getFirst();
        if (!"PENDING".equals(row.get("status"))) throw conflict("이미 처리된 신청입니다.");
        Integer guild = null;
        if ("APPROVED".equals(decision)) {
            long user = ((Number) row.get("applicant_id")).longValue();
            access.lockUser(user);
            ensureFree(user);
            var identity = access.verified(user);
            if (!identity.server().equals(row.get("server"))
                    || !identity.characterName().equals(row.get("character_name")))
                throw conflict("신청 당시 인증 정보와 다릅니다.");
            String name = (String) row.get("name");
            ensureAvailable(identity.server(), name);
            db.update(
                    "INSERT INTO guild(name,server,register_id) VALUES(?,?,?)",
                    name,
                    identity.server(),
                    user);
            guild = db.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
            db.update("INSERT INTO guild_profile(guild_id) VALUES(?)", guild);
            join(guild, user, "MEMBER");
        }
        db.update(
                "INSERT INTO guild_registration_review(request_id,reviewer_id,decision,reason) VALUES(?,?,?,?)",
                id,
                access.user(),
                decision,
                reason);
        db.update(
                "UPDATE guild_registration_request SET status=?,guild_id=? WHERE id=?",
                decision,
                guild,
                id);
    }

    @Transactional
    public void master(int guild, long user) {
        access.requireAdmin();
        access.lockGuild(guild);
        access.lockUser(user);
        String server =
                db.queryForObject("SELECT server FROM guild WHERE id=?", String.class, guild);
        if (!access.verified(user).server().equals(server))
            throw conflict("같은 서버의 인증 사용자를 지정해 주세요.");
        String role = access.role(guild, user);
        if (role == null) ensureFree(user);
        db.update("UPDATE guild_member SET role='MEMBER' WHERE active_master=?", guild);
        if (role == null) join(guild, user, "MASTER");
        else
            db.update(
                    "UPDATE guild_member SET role='MASTER' WHERE guild_id=? AND active_user=?",
                    guild,
                    user);
        db.update("UPDATE guild SET master_id=? WHERE id=?", user, guild);
    }

    @Transactional
    public void role(int guild, long user, String role) {
        access.lockGuild(guild);
        if (!"MASTER".equals(access.role(guild, access.user()))) throw denied();
        if (!"MEMBER".equals(role) && !"DEPUTY".equals(role)) throw invalid();
        String current = access.role(guild, user);
        if (current == null || "MASTER".equals(current)) throw conflict("일반 길드원이나 부마스터를 선택해 주세요.");
        db.update(
                "UPDATE guild_member SET role=? WHERE guild_id=? AND active_user=?",
                role,
                guild,
                user);
    }

    @Transactional
    public void leave(int guild) {
        long user = access.user();
        access.lockGuild(guild);
        access.lockUser(user);
        if (db.update(
                        "UPDATE guild_member SET left_at=CURRENT_TIMESTAMP WHERE guild_id=? AND active_user=?",
                        guild,
                        user)
                != 1) throw denied();
        db.update("UPDATE guild SET master_id=NULL WHERE id=? AND master_id=?", guild, user);
    }

    public List<Map<String, Object>> members(int guild) {
        if (access.role(guild, access.user()) == null) throw denied();
        return db.queryForList(
                "SELECT m.user_id AS userId,u.nickname,m.role FROM guild_member m JOIN user_summary u ON u.id=m.user_id WHERE m.guild_id=? AND m.left_at IS NULL ORDER BY m.id",
                guild);
    }

    private void join(int guild, long user, String role) {
        db.update(
                "INSERT INTO guild_member(guild_id,user_id,role) VALUES(?,?,?)", guild, user, role);
        db.update(
                "UPDATE guild_join_application SET status='CANCELLED',reviewed_at=CURRENT_TIMESTAMP WHERE applicant_id=? AND status='PENDING'",
                user);
    }

    private void ensureFree(long user) {
        if (!db.queryForList("SELECT id FROM guild_member WHERE active_user=?", Long.class, user)
                .isEmpty()) throw conflict("이미 소속 길드가 있습니다.");
    }

    private void ensureAvailable(String server, String name) {
        if (!db.queryForList(
                        "SELECT id FROM guild WHERE server=? AND active_name=?",
                        Integer.class,
                        server,
                        name)
                .isEmpty()) throw conflict("이미 등록된 길드입니다.");
    }

    private static String bounded(String value, int max, boolean required) {
        String s = value == null ? "" : value.strip();
        if (s.length() > max || (required && s.isEmpty())) throw invalid();
        return s;
    }

    private static ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "입력 내용을 확인해 주세요.");
    }

    private static ResponseStatusException denied() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "길드 권한이 필요합니다.");
    }

    private static ResponseStatusException conflict(String text) {
        return new ResponseStatusException(HttpStatus.CONFLICT, text);
    }
}
