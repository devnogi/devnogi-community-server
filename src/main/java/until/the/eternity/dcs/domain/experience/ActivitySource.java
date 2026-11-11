package until.the.eternity.dcs.domain.experience;

/** Closed, server-owned SQL projections shared by baseline scanning and delete snapshots. */
public enum ActivitySource {
    POST(
            "post",
            "POST",
            "POST_PUBLISHED",
            "POST",
            "SELECT p.id source_id,p.id target_id,p.user_id actor_id,p.user_id author_id,p.id root_id,COALESCE(p.first_published_at,p.created_at) occurred_at FROM post p WHERE p.is_deleted=0 AND p.is_draft=0 AND COALESCE(p.is_blocked,0)=0"),
    COMMENT(
            "comment",
            "COMMENT",
            "COMMENT_CREATED",
            "POST",
            "SELECT c.id source_id,c.id target_id,c.user_id actor_id,c.user_id author_id,p.id root_id,c.created_at occurred_at FROM comment c JOIN post p ON p.id=c.post_id WHERE c.is_deleted=0 AND COALESCE(c.is_blocked,0)=0 AND p.is_deleted=0 AND p.is_draft=0 AND COALESCE(p.is_blocked,0)=0"),
    OUTFIT(
            "outfit_post",
            "OUTFIT",
            "OUTFIT_PUBLISHED",
            "OUTFIT",
            "SELECT p.id source_id,p.id target_id,p.user_id actor_id,p.user_id author_id,p.id root_id,p.created_at occurred_at FROM outfit_post p WHERE p.is_deleted=0"),
    OUTFIT_COMMENT(
            "outfit_comment",
            "OUTFIT_COMMENT",
            "COMMENT_CREATED",
            "OUTFIT",
            "SELECT c.id source_id,c.id target_id,c.user_id actor_id,c.user_id author_id,p.id root_id,c.created_at occurred_at FROM outfit_comment c JOIN outfit_post p ON p.id=c.outfit_id WHERE c.is_deleted=0 AND p.is_deleted=0"),
    GUILD_POST(
            "guild_post",
            "GUILD_POST",
            "GUILD_POST_PUBLISHED",
            "GUILD_POST",
            "SELECT p.id source_id,p.id target_id,p.author_id actor_id,p.author_id author_id,p.id root_id,p.created_at occurred_at FROM guild_post p JOIN guild g ON g.id=p.guild_id WHERE g.is_deleted=0 AND p.is_deleted=0"),
    GUILD_RECRUITMENT(
            "guild_recruitment_post",
            "GUILD_RECRUITMENT",
            "GUILD_POST_PUBLISHED",
            "GUILD_RECRUITMENT",
            "SELECT p.id source_id,p.id target_id,p.author_id actor_id,p.author_id author_id,p.id root_id,p.created_at occurred_at FROM guild_recruitment_post p JOIN guild g ON g.id=p.guild_id WHERE g.is_deleted=0 AND p.is_deleted=0"),
    GUILD(
            "guild",
            "GUILD",
            "GUILD_REGISTERED",
            "GUILD",
            "SELECT g.id source_id,g.id target_id,g.register_id actor_id,g.register_id author_id,g.id root_id,g.registered_at occurred_at FROM guild g WHERE g.is_deleted=0"),
    POST_LIKE(
            "post_like",
            "POST",
            "LIKE",
            "POST",
            "SELECT l.id source_id,p.id target_id,l.user_id actor_id,p.user_id author_id,p.id root_id,l.created_at occurred_at FROM post_like l JOIN post p ON p.id=l.post_id WHERE p.is_deleted=0 AND p.is_draft=0 AND COALESCE(p.is_blocked,0)=0 AND l.user_id<>p.user_id"),
    COMMENT_LIKE(
            "comment_like",
            "COMMENT",
            "LIKE",
            "POST",
            "SELECT l.id source_id,c.id target_id,l.user_id actor_id,c.user_id author_id,p.id root_id,l.created_at occurred_at FROM comment_like l JOIN comment c ON c.id=l.comment_id JOIN post p ON p.id=c.post_id WHERE c.is_deleted=0 AND COALESCE(c.is_blocked,0)=0 AND p.is_deleted=0 AND p.is_draft=0 AND COALESCE(p.is_blocked,0)=0 AND l.user_id<>c.user_id"),
    OUTFIT_LIKE(
            "outfit_like",
            "OUTFIT",
            "LIKE",
            "OUTFIT",
            "SELECT l.id source_id,p.id target_id,l.user_id actor_id,p.user_id author_id,p.id root_id,l.created_at occurred_at FROM outfit_like l JOIN outfit_post p ON p.id=l.outfit_id WHERE p.is_deleted=0 AND l.user_id<>p.user_id");

    final String table;
    final String targetType;
    final String activityType;
    final String rootType;
    final String select;

    ActivitySource(
            String table, String targetType, String activityType, String rootType, String select) {
        this.table = table;
        this.targetType = targetType;
        this.activityType = activityType;
        this.rootType = rootType;
        this.select = select;
    }

    String key(long id, long actor) {
        return targetType + ":" + id + (activityType.equals("LIKE") ? ":LIKE:" + actor : ":CREATE");
    }

    String deletionSelect() {
        // Reversal must include previously credited content even after a draft/block edit.
        // Missing parent relations are excluded only after their own prior deletion snapshot.
        return select.substring(0, select.indexOf(" WHERE "));
    }
}
