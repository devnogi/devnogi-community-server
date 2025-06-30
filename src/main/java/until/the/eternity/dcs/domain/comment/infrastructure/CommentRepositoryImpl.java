package until.the.eternity.dcs.domain.comment.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {
	private final JpaCommentRepository jpaRepository;

	@Override
	public Comment save(Comment comment) {
		return jpaRepository.save(comment);
	}

	@Override
	public Optional<Comment> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public void deleteById(Long id) {
		jpaRepository.deleteById(id);
	}
}
