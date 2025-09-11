package until.the.eternity.dcs.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementNotFoundException;
import until.the.eternity.dcs.domain.announcement.infrastructure.JpaAnnouncementRepository;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Component
@RequiredArgsConstructor
public class AnnouncementPermissionEvaluator {
    private final UserSummaryRepository userSummaryRepository;
    private final JpaAnnouncementRepository jpaAnnouncementRepository;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PREFIX = "ROLE_";

    public boolean canDelete(Authentication auth, Long announcementId) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        if (!userSummaryRepository.existsById(currentUserId)) {
            return false;
        }
        Announcement announcement =
                jpaAnnouncementRepository
                        .findById(announcementId)
                        .orElseThrow(() -> new AnnouncementNotFoundException(announcementId));
        if (hasRole(auth, ROLE_ADMIN)) {
            return true;
        }
        return announcement.getUserId().equals(currentUserId);
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public Long getCurrentUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_PREFIX + ROLE_ADMIN));
    }
}
