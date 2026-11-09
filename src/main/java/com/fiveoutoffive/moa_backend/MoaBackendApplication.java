package com.fiveoutoffive.moa_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MoaBackendApplication {

    public static void main(String[] args) {
        // 저장 시각은 UTC 로 통일한다. (API 명세 공통 규칙 "날짜·시간")
        // 커넥션 풀이 만들어지기 전에 지정해야 JDBC 세션도 같은 시간대를 쓴다.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(MoaBackendApplication.class, args);
    }
}
