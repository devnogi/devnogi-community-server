package until.the.eternity.dcs.domain.announcement.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;

public interface JpaAnnouncementRepository extends JpaRepository<Announcement, Long> {

    Boolean existsByPostId(Long postId);

    @Query("SELECT a FROM Announcement a WHERE a.board.id = :boardId OR a.isGlobal = TRUE")
    List<Announcement> findByBoardIdOrGlobalTrue(Long boardId);
}
