package until.the.eternity.dcs.domain.board.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.request.BoardUpdateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.exception.BoardNotFoundException;
import until.the.eternity.dcs.domain.board.infrastructure.BoardRepository;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardConverter boardConverter;
    private final BoardPermissionEvaluator boardPermissionEvaluator;

    @Transactional
    @PreAuthorize("@boardPermissionEvaluator.checkIsAdmin(authentication)")
    public BoardPersistResponse createBoard(BoardCreateRequest request) {
        Long userId = getCurrentUserId();
        Board board = boardConverter.fromCreateRequestToBoard(request, userId);
        Board saved = boardRepository.save(board);
        return boardConverter.fromBoardToPersistResponse(saved);
    }

    public BoardListResponse getAllBoards() {
        List<Board> boardList =
                boardRepository.findAllByIsDeletedIsFalseOrderByTopCategoryAscSubCategoryAsc();
        return boardConverter.fromBoardToListResponse(boardList);
    }

    @Transactional
    @PreAuthorize("@boardPermissionEvaluator.checkIsAdmin(authentication)")
    public BoardPersistResponse updateBoard(Long id, BoardUpdateRequest request) {

        Long userId = getCurrentUserId();
        Board board = findBoardById(id);
        board.update(
                request.name(),
                request.description(),
                request.topCategory(),
                request.subCategory(),
                userId);
        return boardConverter.fromBoardToPersistResponse(board);
    }

    @Transactional
    @PreAuthorize("@boardPermissionEvaluator.checkIsAdmin(authentication)")
    public void deleteBoard(Long id) {
        Long userId = getCurrentUserId();
        Board board = findBoardById(id);
        board.delete(userId);
    }

    public Board findBoardById(Long id) {
        return boardRepository
                .findByIdAndIsDeletedIsFalse(id)
                .orElseThrow(() -> new BoardNotFoundException(id));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return boardPermissionEvaluator.getCurrentUserId(auth);
    }
}
