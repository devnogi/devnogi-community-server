package until.the.eternity.dcs.domain.notice.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.notice.entity.Notice;

public interface JpaNoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByIdIn(List<Long> noticeIds);
}
