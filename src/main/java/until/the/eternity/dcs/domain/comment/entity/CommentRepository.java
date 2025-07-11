package until.the.eternity.dcs.domain.comment.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.comment.infrastructure.JpaCommentRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepository {
	private final JpaCommentRepository jpaRepository;

	public Comment save(Comment comment) {
		return jpaRepository.save(comment);
	}

	public Optional<Comment> findById(Long id) {
		return jpaRepository.findById(id);
	}

	public Page<Comment> findByPost(Long postId, Pageable pageable) {
		return jpaRepository.findAllByPostId(postId, pageable);
	}
}
