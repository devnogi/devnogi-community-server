package until.the.eternity.dcs.common.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import until.the.eternity.dcs.common.util.IpAddressUtil;

class JwtHeaderFilterTest {
    private final JwtHeaderFilter filter = new JwtHeaderFilter(new IpAddressUtil());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validInternalHeadersCreateAuthenticatedPrincipal() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/posts");
        request.addHeader("X-Auth-User-Id", "42");
        request.addHeader("X-Auth-Roles", "USER");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(42L);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "SUPER_ADMIN"})
    void administratorHeadersRemainAuthenticated(String role) throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/comments");
        request.addHeader("X-Auth-User-Id", "42");
        request.addHeader("X-Auth-Roles", role);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_" + role);
    }

    @Test
    void unknownRoleCreatesUnauthenticatedContextInsteadOfThrowing() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/posts");
        request.addHeader("X-Auth-User-Id", "42");
        request.addHeader("X-Auth-Roles", "unknown");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.isAuthenticated()).isFalse();
        assertThat(authentication.getPrincipal()).isNull();
    }

    @Test
    void missingHeadersRemainUnauthenticated() throws Exception {
        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/posts"),
                new MockHttpServletResponse(),
                new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.isAuthenticated()).isFalse();
        assertThat(authentication.getPrincipal()).isNull();
    }
}
