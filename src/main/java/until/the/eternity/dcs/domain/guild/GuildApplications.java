package until.the.eternity.dcs.domain.guild;

import static until.the.eternity.dcs.domain.guild.GuildContent.*;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuildApplications {
    final JdbcTemplate db;
    final GuildAccess access;
    final GuildContent content;
    static final String ROWS =
            "SELECT a.id,a.recruitment_id AS recruitmentId,a.applicant_id AS applicantId,r.guild_id AS guildId,g.name AS guildName,r.title AS recruitmentTitle,a.server,a.character_name AS characterName,a.message,a.status,a.created_at AS createdAt,a.reviewed_at AS reviewedAt FROM guild_join_application a JOIN guild_recruitment_post r ON r.id=a.recruitment_id JOIN guild g ON g.id=r.guild_id ";

    public List<Map<String, Object>> mine() {
        return db.queryForList(ROWS + " WHERE a.applicant_id=? ORDER BY a.id DESC", access.user());
    }

    public List<Map<String, Object>> inbox(int g, long id) {
        content.officer(g);
        content.writing(g, id, false);
        return db.queryForList(ROWS + " WHERE r.guild_id=? AND r.id=? ORDER BY a.id DESC", g, id);
    }

    void eligible(int g, long u, GuildAccess.Identity identity) {
        if (!identity.server()
                .equals(
                        content.one("SELECT server FROM guild WHERE id=? AND is_deleted=0", g)
                                .get("server")))
            throw error(HttpStatus.CONFLICT, "같은 서버의 캐릭터만 지원할 수 있습니다.");
        if (!db.queryForList("SELECT id FROM guild_member WHERE user_id=? AND left_at IS NULL", u)
                .isEmpty()) throw error(HttpStatus.CONFLICT, "이미 길드에 소속되어 있습니다.");
    }

    void open(int g, long id) {
        if (!"OPEN".equals(content.writing(g, id, false).get("status")))
            throw error(HttpStatus.CONFLICT, "모집이 마감되었습니다.");
    }

    @Transactional
    public Map<String, Long> apply(int g, long id, String message) {
        long u = access.user();
        access.lockGuild(g);
        access.lockUser(u);
        open(g, id);
        var character = access.verified(u);
        eligible(g, u, character);
        if (!db.queryForList(
                        "SELECT id FROM guild_join_application WHERE recruitment_id=? AND applicant_id=? AND status='PENDING'",
                        id,
                        u)
                .isEmpty()) throw error(HttpStatus.CONFLICT, "이미 지원했습니다.");
        db.update(
                "INSERT INTO guild_join_application(recruitment_id,applicant_id,server,character_name,message,status) VALUES(?,?,?,?,?,'PENDING')",
                id,
                u,
                character.server(),
                character.characterName(),
                field(message, 1000, false));
        return Map.of(
                "id",
                Objects.requireNonNull(db.queryForObject("SELECT LAST_INSERT_ID()", Long.class)));
    }

    @Transactional
    public void decide(int g, long id, long app, String action) {
        long viewer = access.user();
        access.lockGuild(g);
        if (!"cancel".equals(action)) content.officer(g);
        Map<String, Object> row =
                content.one(ROWS + " WHERE r.guild_id=? AND r.id=? AND a.id=?", g, id, app);
        long applicant = ((Number) row.get("applicantId")).longValue();
        if ("cancel".equals(action) && viewer != applicant)
            throw error(HttpStatus.FORBIDDEN, "본인의 지원만 취소할 수 있습니다.");
        access.lockUser(applicant);
        String status =
                switch (action) {
                    case "accept" -> "ACCEPTED";
                    case "reject" -> "REJECTED";
                    case "cancel" -> "CANCELLED";
                    default -> throw error(HttpStatus.NOT_FOUND, "잘못된 처리입니다.");
                };
        if (db.update(
                        "UPDATE guild_join_application SET status=?,reviewer_id=?,reviewed_at=CURRENT_TIMESTAMP WHERE id=? AND status='PENDING'",
                        status,
                        viewer,
                        app)
                != 1) throw error(HttpStatus.CONFLICT, "이미 처리되었습니다.");
        if ("accept".equals(action)) {
            open(g, id);
            var identity = access.verified(applicant);
            eligible(g, applicant, identity);
            if (!identity.characterName().equals(row.get("characterName"))
                    || !identity.server().equals(row.get("server")))
                throw error(HttpStatus.CONFLICT, "인증 캐릭터가 변경되었습니다.");
            db.update(
                    "INSERT INTO guild_member(guild_id,user_id,role) VALUES(?,?,'MEMBER')",
                    g,
                    applicant);
            db.update(
                    "UPDATE guild_join_application SET status='CANCELLED',reviewed_at=CURRENT_TIMESTAMP WHERE applicant_id=? AND status='PENDING'",
                    applicant);
        }
    }
}
