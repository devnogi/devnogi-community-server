package until.the.eternity.dcs.domain.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

public interface UserSummaryRepository extends JpaRepository<UserSummary, Long> {}
