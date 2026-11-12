package com.fiveoutoffive.moa_backend.global.security;

import com.fiveoutoffive.moa_backend.global.exception.ErrorResponseWriter;
import com.fiveoutoffive.moa_backend.global.exception.GlobalErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 요청마다 JWT를 검증해 SecurityContext에 인증 정보를 채운다.
 *
 * <p>토큰은 {@code Authorization: Bearer} 헤더에서만 읽는다. (API 명세 공통 규칙 '인증')
 * accessToken 쿠키로는 인증하지 않는다. 운영 도메인 moarang.kro.kr 의 kro.kr 은 공개 접미사 목록(PSL)에 없어
 * 브라우저가 다른 *.kro.kr 사이트도 같은 사이트로 보므로, SameSite 쿠키로는 CSRF 를 막을 수 없다.
 * 프론트 BFF(Next 서버)는 Spring 을 부를 때 항상 Bearer 헤더를 붙인다.
 * 쿠키 인증은 WebSocket(/ws) 핸드셰이크에서만 쓰며, 그쪽은 Origin 허용 목록 검사로 막는다. (11/12 회의 결정)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final GlobalJwtProvider globalJwtProvider;
    private final ErrorResponseWriter errorResponseWriter;

    // 로그인/가입/토큰재발급처럼 "기존 세션 상태와 무관하게 항상 동작해야 하는" 엔드포인트.
    // 만료된 액세스 토큰을 붙인 채 호출해도 로그인·재발급 자체가 401로 막히지 않게 한다.
    private static final String PUBLIC_AUTH_PREFIX = "/api/v1/auth/";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith(PUBLIC_AUTH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null) {
                // validateToken(boolean)을 쓰면 만료/위조 사유가 사라져 아래 catch가 죽은 코드가 된다.
                // 클라이언트가 "재발급하면 되는 상황"과 "다시 로그인해야 하는 상황"을 구분할 수 있어야 한다.
                Claims claims = globalJwtProvider.parseClaims(token);
                String subject = claims.getSubject();
                String role = claims.get("role", String.class);

                var authorities = StringUtils.hasText(role)
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        : List.<SimpleGrantedAuthority>of();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료: {}", e.getMessage());
            errorResponseWriter.write(response, GlobalErrorCode.TOKEN_EXPIRED);
            return;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰: {}", e.getMessage());
            errorResponseWriter.write(response, GlobalErrorCode.TOKEN_INVALID);
            return;

        } catch (Exception e) {
            log.error("JWT 인증 처리 중 서버 에러 발생: {}", e.getMessage(), e);
            errorResponseWriter.write(response, GlobalErrorCode.SERVER_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Authorization: Bearer {token} 만 받는다. 쿠키는 보지 않는다(클래스 주석 참고). */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
