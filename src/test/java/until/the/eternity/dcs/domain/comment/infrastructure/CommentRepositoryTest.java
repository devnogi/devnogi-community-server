package until.the.eternity.dcs.domain.comment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import until.the.eternity.dcs.common.config.QueryDslConfig;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.post.entity.Post;

@DataJpaTest
@Import(QueryDslConfig.class)
class CommentRepositoryTest {
    @Autowired CommentRepository commentRepository;
    @Autowired EntityManager entityManager;

    private Board board;

    @BeforeEach
    void setUp() {
        board = Board.builder().name("profile").topCategory("test").subCategory("test").build();
        board.setCreatedAt(LocalDateTime.now());
        entityManager.persist(board);
    }

    @Test
    @DisplayName("사용자 댓글 조회는 댓글과 부모 게시글이 모두 공개인 댓글과 답글만 반환한다")
    void findVisibleByUserIdFiltersCommentAndPostVisibility() {
        long userId = 7L;
        Post visiblePost = createPost("visible", false, false, false);
        Post blockedPost = createPost("blocked", false, true, false);
        Post deletedPost = createPost("deleted", false, false, true);
        Post draftPost = createPost("draft", true, false, false);

        Comment parent = createComment(visiblePost, userId, null, false, false, "parent", 1);
        createComment(visiblePost, userId, parent, false, false, "reply", 2);
        createComment(visiblePost, userId, null, true, false, "deleted-comment", 3);
        createComment(visiblePost, userId, null, false, true, "blocked-comment", 4);
        createComment(blockedPost, userId, null, false, false, "blocked-post", 5);
        createComment(deletedPost, userId, null, false, false, "deleted-post", 6);
        createComment(draftPost, userId, null, false, false, "draft-post", 7);
        createComment(visiblePost, 8L, null, false, false, "other-user", 8);
        entityManager.flush();
        entityManager.clear();

        PageRequest pageable =
                PageRequest.of(0, 20, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        Page<Comment> result = commentRepository.findVisibleByUserId(userId, pageable);

        assertThat(result.getContent())
                .extracting(Comment::getContent)
                .containsExactly("reply", "parent");
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allSatisfy(comment -> assertThat(comment.getPost()).isNotNull());
    }

    @Test
    @DisplayName("사용자 댓글 조회는 작성 시각이 같으면 큰 ID부터 반환한다")
    void findVisibleByUserIdUsesIdAsDescendingTieBreaker() {
        long userId = 7L;
        Post post = createPost("visible", false, false, false);
        Comment first = createComment(post, userId, null, false, false, "first", 1);
        Comment second = createComment(post, userId, null, false, false, "second", 1);
        entityManager.flush();
        entityManager.clear();

        PageRequest pageable =
                PageRequest.of(0, 20, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        Page<Comment> result = commentRepository.findVisibleByUserId(userId, pageable);

        assertThat(result.getContent())
                .extracting(Comment::getId)
                .containsExactly(second.getId(), first.getId());
    }

    private Post createPost(String title, boolean draft, boolean blocked, boolean deleted) {
        Post post =
                Post.builder()
                        .board(board)
                        .userId(1L)
                        .title(title)
                        .content("content")
                        .isDraft(draft)
                        .isBlocked(blocked)
                        .comments(new ArrayList<>())
                        .postTags(new ArrayList<>())
                        .build();
        post.setIsDeleted(deleted);
        post.setCreatedAt(LocalDateTime.now());
        entityManager.persist(post);
        return post;
    }

    private Comment createComment(
            Post post,
            long userId,
            Comment parent,
            boolean deleted,
            boolean blocked,
            String content,
            int createdSecond) {
        Comment comment =
                Comment.builder()
                        .post(post)
                        .userId(userId)
                        .parentComment(parent)
                        .childComments(new ArrayList<>())
                        .content(content)
                        .isBlocked(blocked)
                        .build();
        comment.setIsDeleted(deleted);
        comment.setCreatedAt(LocalDateTime.of(2026, 11, 5, 0, 0, createdSecond));
        entityManager.persist(comment);
        return comment;
    }
}
