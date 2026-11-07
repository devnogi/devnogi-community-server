package until.the.eternity.dcs.domain.guild;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

class GuildAccessTest {
    private final GuildAccess access =
            new GuildAccess(mock(JdbcTemplate.class), "http://localhost");

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void guestCannotRegisterAndMembersCannotApprove() {
        assertEquals(
                401,
                assertThrows(ResponseStatusException.class, access::user).getStatusCode().value());
        signIn("USER");
        assertEquals(
                403,
                assertThrows(ResponseStatusException.class, access::requireAdmin)
                        .getStatusCode()
                        .value());
        signIn("ADMIN");
        assertDoesNotThrow(access::requireAdmin);
        signIn("SUPER_ADMIN");
        assertDoesNotThrow(access::requireAdmin);
    }

    private void signIn(String role) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                1L, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
