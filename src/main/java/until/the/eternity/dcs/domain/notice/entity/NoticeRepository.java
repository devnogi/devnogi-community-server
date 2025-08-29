package until.the.eternity.dcs.domain.notice.entity;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.notice.infrastructure.JpaNoticeRepository;

@Repository
@RequiredArgsConstructor
public class NoticeRepository {
    private final JpaNoticeRepository jpaNoticeRepository;

    public Notice save(Notice notice) {
        return jpaNoticeRepository.save(notice);
    }

    public Optional<Notice> findById(Long id) {
        return jpaNoticeRepository.findById(id);
    }

    public List<Notice> findByIdIn(List<Long> noticeIdList) {
        return jpaNoticeRepository.findByIdIn(noticeIdList);
    }
}
