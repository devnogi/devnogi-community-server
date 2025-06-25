package until.the.eternity.dcs.domain.user.fake;

import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Service
public class FakeUserService {
	public UserSummary getUser() {
		return UserSummary.builder().id(1L).build();
	}
}
