package until.the.eternity.dcs.domain.user.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

public interface UserSummaryRepository extends JpaRepository<UserSummary, Long> {
    Optional<UserSummary> findFirstByOrderByIdDesc();
}
