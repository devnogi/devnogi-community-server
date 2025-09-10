package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException;
import until.the.eternity.dcs.domain.comment.infrastructure.JpaCommentRepository;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Component
@RequiredArgsConstructor
public class CommentPermissionEvaluator {
    private final UserSummaryRepository userSummaryRepository;
    private final JpaCommentRepository jpaCommentRepository;

    public boolean canCreate(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long userId = getCurrentUserId(auth);
        return userSummaryRepository.existsById(userId);
    }

    public boolean canUpdate(Authentication auth, Long id) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        if (!userSummaryRepository.existsById(currentUserId)) {
            return false;
        }
        Comment comment =
                jpaCommentRepository
                        .findById(id)
                        .orElseThrow(() -> new CommentNotFoundException(id));
        Long commentWriter = comment.getUserId();
        return currentUserId.equals(commentWriter);
    }

    public boolean canDelete(Authentication auth, Long id) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        if (!userSummaryRepository.existsById(currentUserId)) {
            return false;
        }
        Comment comment =
                jpaCommentRepository
                        .findById(id)
                        .orElseThrow(() -> new CommentNotFoundException(id));
        Long commentWriter = comment.getUserId();
        return currentUserId.equals(commentWriter);
    }

    public boolean canToggleLike(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long userId = getCurrentUserId(auth);
        return userSummaryRepository.existsById(userId);
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
}
