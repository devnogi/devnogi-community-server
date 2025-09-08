package until.the.eternity.dcs.domain.board.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.exception.BoardNotFoundException;
import until.the.eternity.dcs.domain.board.infrastructure.JpaBoardRepository;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Component
@RequiredArgsConstructor
public class BoardPermissionEvaluator {
    private final JpaBoardRepository jpaBoardRepository;
    private final UserSummaryRepository userSummaryRepository;

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
            return false;
        }
        return hasRole(auth, "ADMIN");
    }

    private Board getBoard(Long boardId) {
        return jpaBoardRepository
                .findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
