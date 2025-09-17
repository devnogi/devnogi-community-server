package until.the.eternity.dcs.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class IpAddressUtil {

    public String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (!isIpFound(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!isIpFound(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!isIpFound(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!isIpFound(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (!isIpFound(ip)) {
            ip = request.getRemoteAddr();
        }

        if (StringUtils.hasText(ip) && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }

        return ip;
    }

    private boolean isIpFound(String ip) {
        return StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip);
    }
}
