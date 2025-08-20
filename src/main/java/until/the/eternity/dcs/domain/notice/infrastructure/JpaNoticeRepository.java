package until.the.eternity.dcs.domain.notice.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.notice.entity.Notice;

public interface JpaNoticeRepository extends JpaRepository<Notice, Long> {}
