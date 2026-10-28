package until.the.eternity.dcs.common.entity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Getter
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String realRemoteAddress;

    public CustomWebAuthenticationDetails(HttpServletRequest request, String realIp) {
        super(request);
        this.realRemoteAddress = realIp;
    }

    @Override
    public String toString() {
        return "CustomWebAuthenticationDetails [RemoteIpAddress(Custom)="
                + realRemoteAddress
                + ", SessionId="
                + getSessionId()
                + ", OriginalRemoteAddress(WebDetails)="
                + super.getRemoteAddress()
                + "]";
    }
}
