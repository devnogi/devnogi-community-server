package until.the.eternity.dcs.domain.board.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.board.entity.Board;

public interface JpaBoardRepository extends JpaRepository<Board, Long> {

    List<Board> findAllByIsDeletedIsFalseOrderByTopCategoryAscSubCategoryAsc();

    Optional<Board> findByIdAndIsDeletedIsFalse(Long id);
}
