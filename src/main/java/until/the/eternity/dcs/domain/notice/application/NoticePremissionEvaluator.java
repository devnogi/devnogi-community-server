package until.the.eternity.dcs.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Component
@RequiredArgsConstructor
public class NoticePremissionEvaluator {
    private final UserSummaryRepository userSummaryRepository;

    public boolean canReadDetail(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        if (isAnonymousUser(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        return userSummaryRepository.existsById(currentUserId);
    }

    public boolean canReadList(Authentication auth, Long userId) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        if (isAnonymousUser(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        if (!userSummaryRepository.existsById(currentUserId)) {
            return false;
        }
        return currentUserId.equals(userId);
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public Long getCurrentUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    public boolean isAnonymousUser(Authentication auth) {
        return auth.getPrincipal().equals("anonymousUser");
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
