package until.the.eternity.dcs.domain.board.entity;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.board.infrastructure.JpaBoardRepository;

@Repository
@RequiredArgsConstructor
public class BoardRepository {
    private final JpaBoardRepository jpaRepository;

    public Board save(Board board) {
        return jpaRepository.save(board);
    }

    public List<Board> findAll() {
        return jpaRepository.findAllByIsDeletedIsFalseOrderByTopCategoryAscSubCategoryAsc();
    }

    public Optional<Board> findById(Long id) {
        return jpaRepository.findByIdAndIsDeletedIsFalse(id);
    }
}
