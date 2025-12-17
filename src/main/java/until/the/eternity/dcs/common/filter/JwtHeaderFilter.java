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

/**
 * Gateway에서 전달한 인증 헤더(X-Auth-*)를 기반으로 Spring Security의 Authentication을 생성하는 필터
 *
 * Gateway에서 전달하는 헤더:
 * - X-Auth-User-Id: 사용자 ID (Long)
 * - X-Auth-Username: 사용자 이메일/username (String)
 * - X-Auth-Roles: 사용자 역할 (예: ROLE_USER, ROLE_ADMIN)
 */
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

        // Gateway에서 전달한 인증 헤더 읽기
        String userIdHeader = request.getHeader("X-Auth-User-Id");
        String usernameHeader = request.getHeader("X-Auth-Username");
        String rolesHeader = request.getHeader("X-Auth-Roles");

        UsernamePasswordAuthenticationToken authentication =
                getAuthentication(userIdHeader, usernameHeader, rolesHeader);

        String clientIp = ipAddressUtil.getClientIp(request);

        CustomWebAuthenticationDetails webAuthenticationDetails =
                new CustomWebAuthenticationDetails(request, clientIp);

        authentication.setDetails(webAuthenticationDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Authentication set for user: {} with roles: {}", usernameHeader, rolesHeader);

        filterChain.doFilter(request, response);
    }

    /**
     * Gateway에서 전달한 헤더 정보를 기반으로 Authentication 생성
     *
     * @param userIdHeader 사용자 ID (Gateway에서 검증됨)
     * @param usernameHeader 사용자 이메일/username (Gateway에서 검증됨)
     * @param rolesHeader 사용자 역할 (Gateway에서 검증됨)
     * @return UsernamePasswordAuthenticationToken
     */
    private UsernamePasswordAuthenticationToken getAuthentication(
            String userIdHeader, String usernameHeader, String rolesHeader) {

        // User ID 파싱
        Long userId = null;
        try {
            if (userIdHeader != null) {
                userId = Long.parseLong(userIdHeader);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid user ID header: {}", userIdHeader);
        }

        // Roles가 없으면 익명 사용자로 처리
        if (rolesHeader == null || rolesHeader.isEmpty()) {
            log.debug("No roles found, creating anonymous authentication");
            return new UsernamePasswordAuthenticationToken(userId, null);
        }

        // Roles 파싱 (ROLE_USER, ROLE_ADMIN 등)
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Gateway에서 넘어온 role이 이미 "ROLE_" prefix를 가지고 있으므로 그대로 사용
        // 단, "ROLE_" prefix가 없으면 추가
        String role = rolesHeader.trim();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        authorities.add(new SimpleGrantedAuthority(role));

        log.debug("Created authentication for userId: {}, username: {}, role: {}",
                  userId, usernameHeader, role);

        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }
}
