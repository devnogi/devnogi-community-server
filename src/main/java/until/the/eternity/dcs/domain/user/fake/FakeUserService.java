package until.the.eternity.dcs.domain.user.fake;

import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

// todo User 관련 실제 동작하는 서비스로 교체 후 Fake는 삭제
@Service
public class FakeUserService implements UserService {
	public UserSummary getCurrentUser() {
		return UserSummary.builder().id(1L).build();
	}
}
