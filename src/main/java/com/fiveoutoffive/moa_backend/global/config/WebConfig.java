package com.fiveoutoffive.moa_backend.global.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginAccountArgumentResolver loginAccountArgumentResolver;
    private final ObjectProvider<AccountGuardInterceptor> accountGuardInterceptorProvider;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 주의: GlobalSecurityConfig의 CORS와 충돌하면
        // "다중 Access-Control-Allow-Origin 헤더" 에러가 발생하므로 비워둔다.
        // CORS 설정은 시큐리티 설정에서 전담한다.
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginAccountArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        accountGuardInterceptorProvider.ifAvailable(interceptor ->
                registry.addInterceptor(interceptor).addPathPatterns("/api/v1/**").excludePathPatterns("/api/v1/auth/**"));
    }
}
