package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.exception.CommentModifyForbiddenException;
import until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.user.exception.UserNotFoundException;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentPermissionEvaluator {
    private final UserSummaryRepository userSummaryRepository;
    private final CommentRepository commentRepository;

    public boolean canCreate(Authentication auth) {
        log.info("comment creat시 auth 값: {}", auth.toString());
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        validateUserExists(currentUserId);

        return true;
    }

    public boolean canUpdate(Authentication auth, Long id) {
        log.info("comment update시 auth 값: {}", auth.toString());
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        validateUserExists(currentUserId);
        Comment comment =
                commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        Long commentWriter = comment.getUserId();
        if (!currentUserId.equals(commentWriter)) {
            throw new CommentModifyForbiddenException();
        }
        return true;
    }

    public boolean canDelete(Authentication auth, Long id) {
        log.info("comment delete시 auth 값: {}", auth.toString());
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        validateUserExists(currentUserId);
        Comment comment =
                commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        Long commentWriter = comment.getUserId();
        if (!currentUserId.equals(commentWriter)) {
            throw new CommentModifyForbiddenException();
        }
        return true;
    }

    public boolean canToggleLike(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        validateUserExists(currentUserId);
        return true;
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    public Long getCurrentUserId(Authentication auth) {
        if (isAnonymousUser(auth)) {
            throw new UserNotFoundException();
        }
        return (Long) auth.getPrincipal();
    }

    public boolean isAnonymousUser(Authentication auth) {
        return auth.getPrincipal() == null;
    }

    public void validateUserExists(Long currentUserId) {
        log.info("validateUserExists currentUserId 값: {}", currentUserId);
        if (!userSummaryRepository.existsById(currentUserId)) {
            log.info("comment 요청시 존재하지 않는 유저");
            throw new UserNotFoundException(currentUserId);
        }
    }
}
