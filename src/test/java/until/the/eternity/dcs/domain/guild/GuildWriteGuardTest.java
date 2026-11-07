package until.the.eternity.dcs.domain.guild;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

class GuildWriteGuardTest {
    @Test
    void cookieAuthenticatedFormCannotCloseRecruitment() {
        var guard = new GuildWriteGuard();
        var request = new MockHttpServletRequest("POST", "/api/guilds/1/recruitments/2/close");
        var response = new MockHttpServletResponse();
        assertEquals(
                403,
                assertThrows(
                                ResponseStatusException.class,
                                () -> guard.preHandle(request, response, this))
                        .getStatusCode()
                        .value());
        request.addHeader("X-Guild-Action", "1");
        assertTrue(guard.preHandle(request, response, this));
    }
}
