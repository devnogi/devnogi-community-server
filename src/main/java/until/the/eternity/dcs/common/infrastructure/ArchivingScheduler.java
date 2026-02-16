package until.the.eternity.dcs.common.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.board.application.BoardArchiveService;
import until.the.eternity.dcs.domain.comment.application.CommentArchiveService;
import until.the.eternity.dcs.domain.post.application.PostArchiveService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ArchivingScheduler {
    private final BoardArchiveService boardArchiveService;
    private final PostArchiveService postArchiveService;
    private final CommentArchiveService commentArchiveService;

    @Scheduled(cron = "0 0 3 * * *")
    public void Schedule() {
        LocalDateTime now = LocalDateTime.now().minusYears(1);
        commentArchiveService.archiveOldComment(now);
        postArchiveService.archiveOldPost(now);
        boardArchiveService.archiveOldBoard(now);
    }
}
