package until.the.eternity.dcs.domain.notice.entity;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.notice.infrastructure.JpaNoticeUserRepository;

@Repository
@RequiredArgsConstructor
public class NoticeUserRepository {
    private final JpaNoticeUserRepository jpaNoticeUserRepository;

    public NoticeUser save(NoticeUser noticeUser) {
        return jpaNoticeUserRepository.save(noticeUser);
    }

    public Optional<NoticeUser> findByNoticeId(Long id) {
        return jpaNoticeUserRepository.findByNoticeId(id);
    }
}
