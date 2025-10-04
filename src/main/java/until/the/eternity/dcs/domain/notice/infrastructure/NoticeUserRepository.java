package until.the.eternity.dcs.domain.notice.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.notice.entity.NoticeUser;

@Repository
public interface NoticeUserRepository extends JpaRepository<NoticeUser, Long> {
    Optional<NoticeUser> findByNoticeId(Long id);

    List<NoticeUser> findByCreatedAtGreaterThanEqualAndUserIdOrderByCreatedAtDesc(
            LocalDateTime date, Long userId);
}
