package until.the.eternity.dcs.domain.post.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Component
@RequiredArgsConstructor
public class PostPermissionEvaluator {
    private final PostRepository postRepository;
    private final UserSummaryRepository userSummaryRepository;

    public boolean canCreate(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        Long currentUserId = getCurrentUserId(auth);
        return userSummaryRepository.existsById(currentUserId);
    }

    public boolean canUpdate(Authentication auth, Long postId) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        if (hasRole(auth, "ADMIN")) {
            return true;
        }
        Long currentUserId = getCurrentUserId(auth);
        if (!userSummaryRepository.existsById(currentUserId)) {
            return false;
        }
        Post currentPost = getPost(postId);

        return currentPost.getUserId().equals(currentUserId);
    }

    public boolean canDelete(Authentication auth, Long postId) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        if (hasRole(auth, "ADMIN")) {
            return true;
        }
        Long currentUserId = getCurrentUserId(auth);
        if (!userSummaryRepository.existsById(currentUserId)) {
            return false;
        }
        Post currentPost = getPost(postId);

        return currentPost.getUserId().equals(currentUserId);
    }

    public boolean canTogglePostLike(Authentication auth) {
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

    private Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
