package until.the.eternity.dcs.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unauthenticatedAuthorizationFailureReturns401() {
        var request = new MockHttpServletRequest("POST", "/api/posts");

        var response =
                handler.handleAccessDenied(
                        new AccessDeniedException("synthetic authorization failure"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("AUTHENTICATION_REQUIRED");
    }

    @Test
    void authenticatedAuthorizationFailureReturns403() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                42L, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        var request = new MockHttpServletRequest("DELETE", "/api/posts/7");

        var response =
                handler.handleAccessDenied(
                        new AccessDeniedException("synthetic authorization failure"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ACCESS_DENIED");
    }
}
