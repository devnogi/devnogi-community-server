package until.the.eternity.dcs.domain.comment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import until.the.eternity.dcs.common.config.QueryDslConfig;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.post.entity.Post;

@DataJpaTest
@Import(QueryDslConfig.class)
class CommentRepositoryTest {
    @Autowired CommentRepository commentRepository;
    @Autowired EntityManager entityManager;

    @Test
    void firstReplyQueryReturnsAtMostThreeRowsPerParentAndCountsAllReplies() {
        Post post =
                Post.builder()
                        .userId(1L)
                        .title("post")
                        .content("content")
                        .comments(new ArrayList<>())
                        .postTags(new ArrayList<>())
                        .build();
        post.setCreatedAt(LocalDateTime.now());
        entityManager.persist(post);
        Comment firstParent = persistComment(post, null, "parent-1", 0);
        Comment secondParent = persistComment(post, null, "parent-2", 0);
        for (int i = 1; i <= 5; i++) {
            persistComment(post, firstParent, "first-" + i, i);
            persistComment(post, secondParent, "second-" + i, i);
        }
        entityManager.flush();
        entityManager.clear();
        List<Long> parentIds = List.of(firstParent.getId(), secondParent.getId());

        List<Long> replyIds =
                commentRepository.findFirst3ReplyIdsByPostIdAndParentIds(post.getId(), parentIds);
        List<Comment> replies = commentRepository.findAllByIdIn(replyIds);
        Map<Long, Long> returnedByParent =
                replies.stream()
                        .collect(
                                Collectors.groupingBy(
                                        reply -> reply.getParentComment().getId(),
                                        Collectors.counting()));
        Map<Long, Long> counts =
                commentRepository.countRepliesByPostIdAndParentIds(post.getId(), parentIds).stream()
                        .collect(
                                Collectors.toMap(
                                        CommentReplyCount::getParentId,
                                        CommentReplyCount::getReplyCount));

        assertThat(replyIds).hasSize(6);
        assertThat(returnedByParent.values()).allMatch(count -> count <= 3);
        assertThat(counts)
                .containsEntry(firstParent.getId(), 5L)
                .containsEntry(secondParent.getId(), 5L);
    }

    private Comment persistComment(Post post, Comment parent, String content, int createdOrder) {
        Comment comment =
                Comment.builder()
                        .post(post)
                        .parentComment(parent)
                        .userId(1L)
                        .content(content)
                        .build();
        comment.setCreatedAt(LocalDateTime.of(2026, 11, 5, 12, 0).plusMinutes(createdOrder));
        entityManager.persist(comment);
        return comment;
    }
}
