package until.the.eternity.dcs.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.user.exception.UserNotFoundException;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Component
@RequiredArgsConstructor
public class NoticePermissionEvaluator {
    private final UserSummaryRepository userSummaryRepository;

    public boolean canRead(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        if (isAnonymousUser(auth)) {
            throw new UserNotFoundException();
        }
        Long currentUserId = getCurrentUserId(auth);
        validateUserExists(currentUserId);
        return true;
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public Long getCurrentUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    public boolean isAnonymousUser(Authentication auth) {
        return auth.getPrincipal() == null;
    }

    public void validateUserExists(Long currentUserId) {
        if (!userSummaryRepository.existsById(currentUserId)) {
            throw new UserNotFoundException(currentUserId);
        }
    }
}
