package until.the.eternity.dcs.domain.board.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.board.exception.BoardModifyForbiddenException;
import until.the.eternity.dcs.domain.user.exception.UserNotFoundException;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Component
@RequiredArgsConstructor
public class BoardPermissionEvaluator {
    private final UserSummaryRepository userSummaryRepository;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PREFIX = "ROLE_";

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public Long getCurrentUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    public boolean checkIsAdmin(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        if (!userSummaryRepository.existsById(currentUserId)) {
            throw new UserNotFoundException(currentUserId);
        }
        if (!hasRole(auth, ROLE_ADMIN)) {
            throw new BoardModifyForbiddenException();
        }
        return true;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_PREFIX + ROLE_ADMIN));
    }
}
