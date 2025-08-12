package until.the.eternity.dcs.domain.comment.entity;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.comment.infrastructure.JpaCommentMetaRepository;

@Repository
@RequiredArgsConstructor
public class CommentMetaRepository {

    private final JpaCommentMetaRepository jpaCommentMetaRepository;

    public Optional<CommentMeta> findById(Long commentId) {
        return jpaCommentMetaRepository.findById(commentId);
    }

    public CommentMeta save(CommentMeta commentMeta) {
        return jpaCommentMetaRepository.save(commentMeta);
    }

    public List<CommentMeta> findByCommentIdIn(List<Long> commentIds) {
        return jpaCommentMetaRepository.findByCommentIdIn(commentIds);
    }
}
