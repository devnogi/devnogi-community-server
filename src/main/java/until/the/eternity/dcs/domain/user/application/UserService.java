package until.the.eternity.dcs.domain.user.application;

import until.the.eternity.dcs.domain.user.entity.UserSummary;

public interface UserService {
    UserSummary getCurrentUser();

    Boolean isAuthenticated();

    /** user_summary 테이블의 저장된 아이디 중 마지막 아이디 */
    Long getLastUserId();
}
