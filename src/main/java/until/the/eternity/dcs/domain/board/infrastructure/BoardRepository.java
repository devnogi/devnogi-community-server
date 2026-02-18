package until.the.eternity.dcs.domain.board.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.board.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findAllByIsDeletedIsFalseOrderByTopCategoryAscSubCategoryAsc();

    Optional<Board> findByIdAndIsDeletedIsFalse(Long id);

    List<Board> findAllByIsDeletedTrueAndDeletedAtLessThanEqual(LocalDateTime date);
}
