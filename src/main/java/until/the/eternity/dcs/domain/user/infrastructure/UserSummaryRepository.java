package until.the.eternity.dcs.domain.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

import java.util.List;
import java.util.Optional;

public interface UserSummaryRepository extends JpaRepository<UserSummary, Long> {
    Optional<UserSummary> findFirstByOrderByIdDesc();

    List<UserSummary> findByIdIn(List<Long> ids);
}
