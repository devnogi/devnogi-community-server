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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import until.the.eternity.dcs.domain.user.enums.UserGrade;
import until.the.eternity.dcs.domain.user.exception.UserGradeNotFoundException;

@Component
@RequiredArgsConstructor
public class JwtHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String userIdCode = request.getHeader("X-USER-ID");
        String userGradeCode = request.getHeader("X-USER-GRADE");

        // 헤더 정보가 없으면 익명 사용자로 처리
        if (userIdCode == null || userGradeCode == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = Long.parseLong(userIdCode);

        UserGrade userGrade =
                UserGrade.fromCode(userGradeCode)
                        .orElseThrow(() -> new UserGradeNotFoundException(userGradeCode));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userGrade.toString()));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
