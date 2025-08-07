package until.the.eternity.dcs.domain.announcement.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;

public interface JpaAnnouncementRepository extends JpaRepository<Announcement, Long> {

    Boolean existsByPostId(Long postId);

    List<Announcement> findByBoardIdOrIsGlobalTrue(Long boardId);
}
