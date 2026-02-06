package until.the.eternity.dcs.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import until.the.eternity.dcs.common.entity.CustomWebAuthenticationDetails;
import until.the.eternity.dcs.common.util.IpAddressUtil;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHeaderFilter extends OncePerRequestFilter {

    private final IpAddressUtil ipAddressUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String userIdCode = request.getHeader("X-USER-ID");
        String userGradeCode = request.getHeader("X-USER-GRADE");

        log.debug(
                "JWT Header Parsing - X-USER-ID: {}, X-USER-GRADE: {}", userIdCode, userGradeCode);

        UsernamePasswordAuthenticationToken authentication =
                getAuthentication(userIdCode, userGradeCode);

        String clientIp = ipAddressUtil.getClientIp(request);

        log.debug("Client IP: {}", clientIp);

        CustomWebAuthenticationDetails webAuthenticationDetails =
                new CustomWebAuthenticationDetails(request, clientIp);

        authentication.setDetails(webAuthenticationDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug(
                "Authentication set - Principal: {}, Authorities: {}",
                authentication.getPrincipal(),
                authentication.getAuthorities());

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(
            String userIdCode, String userGradeCode) {
        Long userId;
        try {
            userId = Long.parseLong(userIdCode);
            log.debug("Parsed userId: {}", userId);
        } catch (NumberFormatException e) {
            log.debug("Failed to parse userIdCode: '{}', setting userId to null", userIdCode);
            userId = null;
        }
        if (userGradeCode == null) {
            log.debug("userGradeCode is null, returning authentication without authorities");
            return new UsernamePasswordAuthenticationToken(userId, null);
        }
        UserGrade userGrade = UserGrade.fromCode(userGradeCode).orElse(null);
        log.debug("Parsed userGrade: {} from code: {}", userGrade, userGradeCode);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userGrade.toString()));
        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }
}
