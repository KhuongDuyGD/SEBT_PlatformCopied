package project.swp.spring.sebt_platform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Simple logging filter to trace all incoming requests (method, path, origin) including
 * CORS preflight (OPTIONS) requests. Helps diagnose CORS failures where browser reports
 * non-OK preflight status.
 */
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String origin = request.getHeader("Origin");
        String acrm = request.getHeader("Access-Control-Request-Method");
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("[CORS-Preflight] method=OPTIONS target={} origin={} reqMethodHeader={}", uri, origin, acrm);
        } else {
            log.debug("[REQ] {} {} origin={}", method, uri, origin);
        }
        filterChain.doFilter(request, response);
    }
}
