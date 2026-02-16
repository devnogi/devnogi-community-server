package until.the.eternity.dcs.domain.announcement.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Boolean existsByPostId(Long postId);

    List<Announcement> findByIsDraftFalseAndBoardIdOrIsDraftFalseAndIsGlobalTrue(Long boardId);

    List<Announcement> findByIsDraftFalseAndIsGlobalTrue();
}
