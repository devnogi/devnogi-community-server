package until.the.eternity.dcs.common.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.board.application.BoardArchiveService;
import until.the.eternity.dcs.domain.comment.application.CommentArchiveService;
import until.the.eternity.dcs.domain.post.application.PostArchiveService;

@Component
@RequiredArgsConstructor
public class ArchivingScheduler {
    private final BoardArchiveService boardArchiveService;
    private final PostArchiveService postArchiveService;
    private final CommentArchiveService commentArchiveService;
}
