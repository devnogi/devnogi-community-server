package until.the.eternity.dcs.domain.guild;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

class GuildVerificationContractTest {
    private final JdbcTemplate db = mock(JdbcTemplate.class);
    private final AtomicReference<String> payload = new AtomicReference<>();
    private HttpServer server;
    private GuildAccess access;

    @BeforeEach
    void startAuthFixture() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(
                "/api/user/verification/public/users/42/summary",
                exchange -> {
                    byte[] body = payload.get().getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, body.length);
                    try (var output = exchange.getResponseBody()) {
                        output.write(body);
                    }
                });
        server.start();
        access = new GuildAccess(db, "http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterEach
    void stopAuthFixture() {
        server.stop(0);
    }

    @Test
    void onlyUsesAnAuthenticatedServerAndCharacterWithAMatchingCommunityUser() {
        payload.set(
                """
            {"success":true,"data":{"userId":42,"verified":true,"serverName":"류트","characterName":"에린"}}
            """);
        when(db.queryForList(anyString(), eq(Long.class), eq(42L), eq("류트"))).thenReturn(List.of());
        assertEquals(
                409,
                assertThrows(ResponseStatusException.class, () -> access.verified(42L))
                        .getStatusCode()
                        .value());
        when(db.queryForList(anyString(), eq(Long.class), eq(42L), eq("류트")))
                .thenReturn(List.of(42L));
        assertEquals(new GuildAccess.Identity("류트", "에린"), access.verified(42L));
        payload.set(
                """
            {"success":true,"data":{"userId":42,"verified":false,"serverName":"류트","characterName":"에린"}}
            """);
        assertEquals(
                403,
                assertThrows(ResponseStatusException.class, () -> access.verified(42L))
                        .getStatusCode()
                        .value());
        payload.set(
                """
            {"success":true,"data":{"userId":7,"verified":true,"serverName":"류트","characterName":"에린"}}
            """);
        assertEquals(
                503,
                assertThrows(ResponseStatusException.class, () -> access.verified(42L))
                        .getStatusCode()
                        .value());
    }
}
