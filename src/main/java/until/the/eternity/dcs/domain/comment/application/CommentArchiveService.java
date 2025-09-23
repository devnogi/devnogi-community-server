package until.the.eternity.dcs.domain.comment.application;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentArchive;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentArchiveRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.JpaCommentLikeRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.JpaCommentMetaRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.JpaCommentRepository;

@Service
@RequiredArgsConstructor
public class CommentArchiveService {
    private final CommentArchiveRepository commentArchiveRepository;
    private final JpaCommentRepository jpaCommentRepository;
    private final JpaCommentLikeRepository jpaCommentLikeRepository;
    private final JpaCommentMetaRepository jpaCommentMetaRepository;

    @Transactional
    public void archiveOldComment(LocalDateTime date) {
        List<Comment> commentList =
                jpaCommentRepository.findAllByIsDeletedTrueAndDeletedAtLessThanEqual(date);
        if (commentList.isEmpty()) {
            return;
        }
        List<Long> commentIdList =
                commentList.stream().map(Comment::getId).collect(Collectors.toList());
        List<CommentArchive> commentArchiveList =
                commentList.stream().map(CommentArchive::from).collect(Collectors.toList());
        commentArchiveRepository.saveAll(commentArchiveList);
        jpaCommentLikeRepository.deleteAllByCommentIdIn(commentIdList);
        jpaCommentMetaRepository.deleteAllByCommentIdIn(commentIdList);
        jpaCommentRepository.deleteAllByCommentIdIn(commentIdList);
    }
}
