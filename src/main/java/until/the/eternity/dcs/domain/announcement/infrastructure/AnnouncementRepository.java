package until.the.eternity.dcs.domain.announcement.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Boolean existsByPostId(Long postId);

    List<Announcement> findByIsDraftFalseAndIsGlobalTrue();

    @Query(
            "SELECT a FROM Announcement a "
                    + "WHERE a.isDraft = false AND (a.isGlobal = true OR a.board.id = :boardId)")
    List<Announcement> findActiveByBoardIdOrGlobal(@Param("boardId") Long boardId);
}
