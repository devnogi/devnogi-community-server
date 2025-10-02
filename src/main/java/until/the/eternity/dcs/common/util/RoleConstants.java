package until.the.eternity.dcs.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleConstants {
    ROLE_ADMIN("ADMIN"),
    ROLE_PREFIX("ROLE_");

    private final String value;
}
