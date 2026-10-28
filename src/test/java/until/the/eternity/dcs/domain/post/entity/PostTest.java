package until.the.eternity.dcs.domain.post.entity;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

public class PostTest {
    Post post;
    String title = "test title";
    String content = "test content";
    boolean isDraft = true;
    List<PostTag> postTags = new ArrayList<>();

    @BeforeEach
    void setUp() {
        post =
                Post.builder()
                        .id(1L)
                        .title(title)
                        .content(content)
                        .isDraft(isDraft)
                        .postTags(postTags)
                        .userId(1L)
                        .build();
    }

    @Test
    @DisplayName("update는 post를 수정한다.")
    void update_Success() {
        // given
        String newTitle = "new test title";
        String newContent = "new test content";
        boolean newIsDraft = false;
        PostTag postTag = PostTag.builder().id(1L).build();
        List<PostTag> newPostTags = new ArrayList<>();
        newPostTags.add(postTag);

        // when
        post.update(newTitle, newContent, newIsDraft, newPostTags, 1L);

        // then
        Assertions.assertEquals(post.getTitle(), newTitle);
        Assertions.assertEquals(post.getContent(), newContent);
        Assertions.assertEquals(post.getIsDraft(), newIsDraft);
        Assertions.assertEquals(post.getPostTags(), newPostTags);
    }

    @Test
    @DisplayName("update는 post를 부분 수정한다.")
    void update_Partial_Success() {
        // given
        String newTitle = "new test title";
        String newContent = "new test content";
        boolean newIsDraft = false;
        PostTag postTag = PostTag.builder().id(1L).build();
        List<PostTag> newPostTags = new ArrayList<>();
        newPostTags.add(postTag);

        // when
        post.update(newTitle, null, null, null, 1L);

        // then
        Assertions.assertEquals(post.getTitle(), newTitle);

        // when
        post.update(null, newContent, null, null, 1L);
        Assertions.assertEquals(post.getContent(), newContent);

        // when
        post.update(null, null, newIsDraft, null, 1L);
        Assertions.assertEquals(post.getIsDraft(), newIsDraft);

        // when
        post.update(null, null, null, newPostTags, 1L);
        Assertions.assertEquals(post.getPostTags(), newPostTags);
    }
}
