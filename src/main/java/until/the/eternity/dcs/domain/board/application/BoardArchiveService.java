package until.the.eternity.dcs.domain.board.application;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.entity.BoardArchive;
import until.the.eternity.dcs.domain.board.infrastructure.BoardArchiveRepository;
import until.the.eternity.dcs.domain.board.infrastructure.JpaBoardRepository;

@Service
@RequiredArgsConstructor
public class BoardArchiveService {
    private final BoardArchiveRepository boardArchiveRepository;
    private final JpaBoardRepository jpaBoardRepository;

    @Transactional
    public void archiveOldBoard(LocalDateTime date) {
        List<Board> boardList =
                jpaBoardRepository.findAllByIsDeletedTrueAndDeletedAtLessThanEqual(date);
        if (boardList.isEmpty()) {
            return;
        }
        List<BoardArchive> boardArchiveList =
                boardList.stream().map(BoardArchive::from).collect(Collectors.toList());
        boardArchiveRepository.saveAll(boardArchiveList);
        jpaBoardRepository.deleteAll(boardList);
    }
}
