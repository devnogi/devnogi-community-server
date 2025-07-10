package until.the.eternity.dcs.domain.comment.application;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentUpdateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentLikeRepository;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.fake.FakeUserService;
import until.the.eternity.dcs.infrastructure.FakeCommentLikeRepository;
import until.the.eternity.dcs.infrastructure.FakeCommentRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommentServiceTest {
	static CommentService commentService;
	static FakeCommentRepository commentRepository;
	static Long id = 1L;
	static String content = "content";

	@BeforeAll
	static void setUp() {
		commentRepository = new FakeCommentRepository();
		CommentConverter commentConverter = new CommentConverter(commentRepository);
		UserService userService = new FakeUserService();
		CommentLikeRepository commentLikeRepository = new FakeCommentLikeRepository();
		CommentLikeConverter commentLikeConverter = new CommentLikeConverter();
		commentService = new CommentService(commentRepository, commentConverter, userService,
			commentLikeRepository, commentLikeConverter);
	}

	@BeforeEach
	void init() {
		commentRepository.clearDbForTest();

		Post post = Post.builder().id(id).build();
		Comment comment = Comment
			.builder()
			.id(id)
			.post(post)
			.userId(id)
			.parentComment(null)
			.childComments(new ArrayList<>())
			.content(content)
			.build();

		commentRepository.save(comment);
	}

	@Test
	@DisplayName("create 는 새로운 comment 를 생성힌다.")
	void create_Success() {
		// given
		CommentCreateRequest request = new CommentCreateRequest(null, content);

		// when
		CommentPersistResponse response = commentService.create(id, request);

		// then
		assertNotNull(response);
	}

	@Test
	@DisplayName("update 는 comment 의 content 를 수정한다.")
	void update_Success() {
		// given
		String newContent = "new content";
		CommentUpdateRequest request = new CommentUpdateRequest(newContent);

		// when
		CommentPersistResponse response = commentService.update(id, request);

		// then
		assertNotNull(response);
		assertEquals(id, response.id());
		Comment comment = commentRepository.findById(id).get();
		assertEquals(newContent, comment.getContent());
	}

	@Test
	@DisplayName("delete 는 comment 를 삭제 처리한다.")
	void delete_Success() {
		// when
		commentService.delete(id);

		// then
		Optional<Comment> comment = commentRepository.findById(id);
		assertEquals(Optional.empty(), comment);
	}

	@Test
	@DisplayName("없는 ID로 comment 를 조회 시 CommentNotFoundException 를 반환한다.")
	void delete_throws_CommentNotFoundException() {
		// given
		Long notExist = 999L;

		// when
		// then
		assertThatThrownBy(() -> commentService.delete(notExist))
			.isInstanceOf(CommentNotFoundException.class);
	}

	@Test
	@DisplayName("findByPostId 는 postId 로 comment 를 페이징 조회한다.")
	void findByPostId() {
		// given
		CustomPageRequest request = new CustomPageRequest(1, 10, "createdAt", "desc");

		// when
		Page<CommentPageResponseItem> response = commentService.findByPostId(id, request);

		// then
		assertNotNull(response);
		assertEquals(1, response.getTotalElements());
		assertEquals(1, response.getTotalPages());
		assertEquals(1, response.getNumberOfElements());
		assertEquals(1, response.getContent().size());
		assertEquals(content, response.getContent().get(0).content());
	}
}