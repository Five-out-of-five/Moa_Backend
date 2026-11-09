package com.fiveoutoffive.moa_backend.global.config;

import com.fiveoutoffive.moa_backend.global.port.out.AccountGuardPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 요청마다 계정 상태를 확인한다.
 * 규칙은 계정 도메인의 {@link AccountGuardPort} 가 구현한다. 인증되지 않은 요청은 시큐리티가 처리하므로 통과시킨다.
 */
@Component
@RequiredArgsConstructor
public class AccountGuardInterceptor implements HandlerInterceptor {

    private final AccountGuardPort accountGuardPort;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }
        try {
            accountGuardPort.onAuthenticatedRequest(Long.valueOf(authentication.getName()));
        } catch (NumberFormatException e) {
            return true; // 익명 사용자는 이름이 anonymousUser 다
        }
        return true;
    }
}
