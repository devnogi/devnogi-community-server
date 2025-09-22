package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentArchiveRepository;

@Service
@RequiredArgsConstructor
public class CommentArchiveService {
    private final CommentArchiveRepository commentArchiveRepository;
}
