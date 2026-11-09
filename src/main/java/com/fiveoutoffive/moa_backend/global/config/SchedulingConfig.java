package com.fiveoutoffive.moa_backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 자동 마감·만료 정리·AI 서버 헬스 체크 등 @Scheduled 작업을 켠다. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
