package com.fiveoutoffive.moa_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// docker compose 의 PostgreSQL·Redis·MinIO 가 떠 있어야 한다.
@SpringBootTest
@ActiveProfiles("local")
class MoaBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
