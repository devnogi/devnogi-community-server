package until.the.eternity.dcs.domain.board.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.board.infrastructure.BoardArchiveRepository;

@Service
@RequiredArgsConstructor
public class BoardArchiveService {
    private final BoardArchiveRepository boardArchiveRepository;
}
