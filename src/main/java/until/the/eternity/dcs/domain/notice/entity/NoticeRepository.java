package until.the.eternity.dcs.domain.notice.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.notice.infrastructure.JpaNoticeRepository;

@Repository
@RequiredArgsConstructor
public class NoticeRepository {
    private final JpaNoticeRepository jpaNoticeRepository;
}
