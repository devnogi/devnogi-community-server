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
import until.the.eternity.dcs.domain.comment.infrastructure.CommentLikeRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentMetaRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentArchiveService {
    private final CommentArchiveRepository commentArchiveRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentMetaRepository commentMetaRepository;

    @Transactional
    public void archiveOldComment(LocalDateTime date) {
        List<Comment> commentList =
                commentRepository.findAllByIsDeletedTrueAndDeletedAtLessThanEqual(date);
        if (commentList.isEmpty()) {
            return;
        }
        List<Long> commentIdList =
                commentList.stream().map(Comment::getId).collect(Collectors.toList());
        List<CommentArchive> commentArchiveList =
                commentList.stream().map(CommentArchive::from).collect(Collectors.toList());
        commentArchiveRepository.saveAll(commentArchiveList);
        commentLikeRepository.deleteAllByCommentIdIn(commentIdList);
        commentMetaRepository.deleteAllByCommentIdIn(commentIdList);
        commentRepository.deleteAllByCommentIdIn(commentIdList);
    }
}
