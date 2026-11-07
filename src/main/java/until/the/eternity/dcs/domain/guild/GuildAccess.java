package until.the.eternity.dcs.domain.guild;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GuildAccess {
    private final JdbcTemplate jdbc;
    private final RestClient auth;

    public record Identity(String server, String characterName) {}

    public GuildAccess(
            JdbcTemplate jdbc, @Value("${AUTH_SERVER_URL:http://localhost:8091}") String baseUrl) {
        this.jdbc = jdbc;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.auth = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public Long viewer() {
        var authn = SecurityContextHolder.getContext().getAuthentication();
        return authn != null && authn.isAuthenticated() && authn.getPrincipal() instanceof Long id
                ? id
                : null;
    }

    public long user() {
        Long id = viewer();
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        return id;
    }

    public void requireAdmin() {
        user();
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .noneMatch(
                        a ->
                                "ROLE_ADMIN".equals(a.getAuthority())
                                        || "ROLE_SUPER_ADMIN".equals(a.getAuthority())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");
    }

    public String role(int guild, long user) {
        return jdbc
                .queryForList(
                        "SELECT role FROM guild_member WHERE guild_id=? AND active_user=?",
                        String.class,
                        guild,
                        user)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public void lockGuild(int guild) {
        if (jdbc.queryForList(
                        "SELECT id FROM guild WHERE id=? AND is_deleted=false FOR UPDATE",
                        Integer.class,
                        guild)
                .isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "길드를 찾을 수 없습니다.");
    }

    public void lockUser(long user) {
        if (jdbc.queryForList("SELECT id FROM user_summary WHERE id=? FOR UPDATE", Long.class, user)
                .isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "회원 정보 동기화 후 다시 시도해 주세요.");
    }

    public Identity verified(long user) {
        JsonNode response;
        try {
            response =
                    auth.get()
                            .uri("/api/user/verification/public/users/{id}/summary?limit=1", user)
                            .retrieve()
                            .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "캐릭터 인증 정보를 확인할 수 없습니다.", e);
        }
        if (response == null
                || !response.path("success").asBoolean()
                || response.path("data").path("userId").asLong(-1) != user)
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "캐릭터 인증 응답을 확인할 수 없습니다.");
        var data = response.path("data");
        String server = data.path("serverName").asText(""),
                character = data.path("characterName").asText("");
        if (!data.path("verified").asBoolean() || server.isBlank() || character.isBlank())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "인게임 인증이 필요합니다.");
        if (jdbc.queryForList(
                        "SELECT id FROM user_summary WHERE id=? AND server_name=? AND is_verified=true",
                        Long.class,
                        user,
                        server)
                .isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "인증 정보 동기화 후 다시 시도해 주세요.");
        return new Identity(server, character);
    }
}
