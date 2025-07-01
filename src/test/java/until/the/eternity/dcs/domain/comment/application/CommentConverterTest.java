package until.the.eternity.dcs.domain.comment.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentRepository;
import until.the.eternity.dcs.infrastructure.FakeCommentRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommentConverterTest {
	private static CommentConverter commentConverter;
	static Long id = 1L;
	static String content = "content";

	@BeforeAll
	static void setUp() {
		CommentRepository commentRepository = new FakeCommentRepository();
		commentConverter = new CommentConverter(commentRepository);
	}

	@Test
	@DisplayName("CommentCreateRequest 에서 Comment 로 변환할 수 있다.")
	void fromCreateRequestToComment() {
		// given
		CommentCreateRequest request = new CommentCreateRequest(null, content);

		// when
		Comment comment = commentConverter.fromCreateRequestToComment(request, id, id);

		// then
		assertNotNull(comment);
		assertEquals(content, comment.getContent());
		assertEquals(id, comment.getPost().getId());
		assertEquals(id, comment.getUserId());
	}

	@Test
	@DisplayName("Comment 에서 CommentPersistResponse 로 변환할 수 있다.")
	void fromCommentToPersistResponse() {
		// given
		Comment comment = Comment.builder()
			.id(id)
			.content(content)
			.build();

		// when
		CommentPersistResponse response = commentConverter.fromCommentToPersistResponse(comment);

		// then
		assertNotNull(response);
		assertEquals(id, response.id());
	}

	@Test
	@DisplayName("Comment 에서 CommentPageResponseItem 로 변환할 수 있다.")
	void fromCommentToPageResponse_Success() {
		// given
		Comment comment = Comment.builder()
			.id(id)
			.userId(id)
			.parentComment(null)
			.content(content)
			.likeCount(0)
			.build();

		// when
		CommentPageResponseItem response = commentConverter.fromCommentToPageResponse(comment);

		// then
		assertNotNull(response);
		assertEquals(id, response.id());
		assertEquals(id, response.userId());
		assertNull(response.parentComment());
		assertEquals(content, response.content());
		assertEquals(0, response.likeCount());
	}





}