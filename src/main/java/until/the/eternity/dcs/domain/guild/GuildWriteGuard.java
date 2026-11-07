package until.the.eternity.dcs.domain.guild;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GuildWriteGuard implements WebMvcConfigurer, HandlerInterceptor {
    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/api/guilds/**", "/api/admin/guilds/**");
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Browser forms cannot supply this header; cross-origin scripts must pass the CORS
        // preflight.
        if (!READ_METHODS.contains(request.getMethod())
                && !"1".equals(request.getHeader("X-Guild-Action"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "길드 화면에서 다시 시도해 주세요.");
        }
        return true;
    }
}
