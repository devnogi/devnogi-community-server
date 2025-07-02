package until.the.eternity.dcs.domain.comment.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.post.entity.Post;

class CommentTest {
	static Comment comment;
	static String content = "content";
	@BeforeEach
	void setUp() {
		Post post = Post.builder().id(1L).build();

		comment = Comment.builder()
			.id(1L)
			.userId(1L)
			.post(post)
			.content(content)
			.build();
	}


	@Test
	@DisplayName("update 는 content 를 수정한다.")
	void update_Success() {
		// given
		String newContent = "newContent";

		// when
		comment.update(newContent, 1L);

		// then
		Assertions.assertEquals(newContent, comment.getContent());
	}

}