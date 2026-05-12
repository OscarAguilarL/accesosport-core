package com.accesosport.auth.infrastructure.security;

import com.accesosport.registration.domain.model.CheckinToken;
import com.accesosport.registration.domain.repository.CheckinTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckinTokenAuthenticationFilter extends OncePerRequestFilter {

    private final CheckinTokenRepository checkinTokenRepository;

    private static final Pattern EVENT_ID_PATTERN = Pattern.compile("/api/v1/events/([0-9a-fA-F-]{36})/");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tokenParam = request.getParameter("token");
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(tokenParam) && !StringUtils.hasText(authHeader)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Optional<CheckinToken> checkinToken = checkinTokenRepository.findByToken(tokenParam);
                if (checkinToken.isPresent() && !checkinToken.get().isExpired()) {
                    CheckinToken ct = checkinToken.get();

                    UUID pathEventId = extractEventIdFromPath(request.getRequestURI());
                    if (pathEventId == null || pathEventId.equals(ct.getEventId())) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                "checkin-agent",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_CHECKIN_AGENT"))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to authenticate checkin token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private UUID extractEventIdFromPath(String uri) {
        Matcher m = EVENT_ID_PATTERN.matcher(uri);
        if (m.find()) {
            try {
                return UUID.fromString(m.group(1));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
