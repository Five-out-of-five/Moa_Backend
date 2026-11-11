package com.fiveoutoffive.moa_backend.support;

import com.fiveoutoffive.moa_backend.global.security.GlobalJwtProvider;
import com.fiveoutoffive.moa_backend.room.application.command.RoomCreateCommand;
import com.fiveoutoffive.moa_backend.room.application.usecase.RoomUseCase;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * BE-2 API 통합 테스트 공통 기반.
 *
 * <p>실행 조건: 로컬 PostgreSQL(moa DB, pgvector 설치)과 Redis 가 떠 있어야 한다. (docs/local-setup.md)
 *
 * <p>개발용 데이터와 섞이지 않도록 같은 moa DB 안의 {@code be2_test} 스키마에 Flyway 로 테이블을 따로 만든다.
 * 테스트를 시작할 때 이 스키마의 데이터를 한 번 비우고, 끝난 뒤에는 남겨 둔다.
 * 마지막 실행 결과는 pgAdmin 에서 moa → Schemas → be2_test 로 확인할 수 있다.
 *
 * <p>Redis 는 개발용과 키가 겹치지 않도록 15번 DB 를 쓴다. AI 서버는 닫힌 포트로 지정해 항상 "꺼진 상태"로 시험한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(properties = {
        // DB 주소(DB_URL)는 각자 설정 그대로 두고, 연결마다 검색 경로만 be2_test 로 바꾼다. vector 타입은 public 에 있다.
        "spring.datasource.hikari.connection-init-sql=SET search_path TO " + Be2ApiTestSupport.SCHEMA + ", public",
        "spring.flyway.schemas=" + Be2ApiTestSupport.SCHEMA,
        "spring.flyway.default-schema=" + Be2ApiTestSupport.SCHEMA,
        "spring.data.redis.database=15",
        "app.ai.server-url=http://localhost:1",
        "logging.level.org.hibernate.SQL=warn"
})
public abstract class Be2ApiTestSupport {

    public static final String SCHEMA = "be2_test";

    private static boolean schemaCleared;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    private GlobalJwtProvider jwtProvider;

    @Autowired
    private RoomUseCase roomUseCase;

    @BeforeEach
    void clearSchemaOnce() {
        synchronized (Be2ApiTestSupport.class) {
            if (schemaCleared) {
                return;
            }
            List<String> tables = jdbc.queryForList(
                    "SELECT tablename FROM pg_tables WHERE schemaname = ? AND tablename <> 'flyway_schema_history'",
                    String.class, SCHEMA);
            if (!tables.isEmpty()) {
                jdbc.execute("TRUNCATE TABLE " + String.join(", ", tables.stream().map(t -> SCHEMA + "." + t).toList()) + " CASCADE");
            }
            schemaCleared = true;
        }
    }

    // ---------- 테스트 데이터 ----------

    /** 가입이 끝난 일반 회원을 만든다. */
    protected Member newMember(String name) {
        Long id = jdbc.queryForObject("""
                INSERT INTO account(provider, provider_uid, email, name, terms_agreed_at, privacy_agreed_at)
                VALUES ('GOOGLE', ?, ?, ?, now(), now()) RETURNING id
                """, Long.class, UUID.randomUUID().toString(), UUID.randomUUID() + "@be2.test", name);
        return new Member(id, name, jwtProvider.createAccessToken(String.valueOf(id), "USER"));
    }

    /** owner 가 방장인 방을 만들고 members 를 멤버로 넣는다. 참여 승인 절차(BE-1)는 거치지 않고 행을 바로 넣는다. */
    protected Long newRoom(Member owner, Member... members) {
        Long roomId = roomUseCase.create(owner.id(), new RoomCreateCommand("테스트방", "BE-2 통합 테스트"), null).roomId();
        for (Member m : members) {
            jdbc.update("""
                    INSERT INTO room_member(room_id, account_id, role, status, nickname, last_briefing_checked_at)
                    VALUES (?, ?, 'MEMBER', 'ACTIVE', ?, now() AT TIME ZONE 'UTC')
                    """, roomId, m.id(), m.name());
        }
        return roomId;
    }

    // ---------- 요청 ----------

    protected Response get(Member who, String path) throws Exception {
        return send(who, HttpMethod.GET, path, null);
    }

    protected Response post(Member who, String path, String json) throws Exception {
        return send(who, HttpMethod.POST, path, json);
    }

    protected Response post(Member who, String path) throws Exception {
        return send(who, HttpMethod.POST, path, null);
    }

    protected Response put(Member who, String path, String json) throws Exception {
        return send(who, HttpMethod.PUT, path, json);
    }

    protected Response patch(Member who, String path, String json) throws Exception {
        return send(who, HttpMethod.PATCH, path, json);
    }

    protected Response delete(Member who, String path) throws Exception {
        return send(who, HttpMethod.DELETE, path, null);
    }

    protected Response send(Member who, HttpMethod method, String path, String json) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.request(method, "/api/v1" + path);
        if (who != null) {
            req.header("Authorization", "Bearer " + who.token());
        }
        if (json != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(json.getBytes(StandardCharsets.UTF_8));
        }
        var result = mockMvc.perform(req).andReturn().getResponse();
        result.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return new Response(result.getStatus(), result.getContentAsString());
    }

    // ---------- 값 ----------

    public record Member(Long id, String name, String token) {
    }

    public record Response(int status, String body) {

        public <T> T read(String jsonPath) {
            return JsonPath.read(body, jsonPath);
        }

        public Long id(String jsonPath) {
            Number n = JsonPath.read(body, jsonPath);
            return n.longValue();
        }

        /** 필터 경로처럼 목록이 나오는 경로에서 첫 번째 숫자 값을 꺼낸다. */
        public Long first(String jsonPath) {
            List<Number> values = JsonPath.read(body, jsonPath);
            if (values.isEmpty()) {
                throw new AssertionError(jsonPath + " 에 해당하는 값이 없습니다: " + body);
            }
            return values.get(0).longValue();
        }

        public String errorCode() {
            return body.contains("\"error\"") ? JsonPath.read(body, "$.error.code") : null;
        }

        /** 성공(200, success=true)이 아니면 응답 본문을 담아 실패시킨다. */
        public Response ok() {
            if (status != 200 || !Boolean.TRUE.equals(JsonPath.read(body, "$.success"))) {
                throw new AssertionError("성공 응답을 기대했지만 " + status + " " + body);
            }
            return this;
        }

        /** 지정한 상태 코드와 에러 코드인지 확인한다. */
        public Response fails(int expectedStatus, String expectedCode) {
            if (status != expectedStatus || !expectedCode.equals(errorCode())) {
                throw new AssertionError("기대: " + expectedStatus + " " + expectedCode + " / 실제: " + status + " " + body);
            }
            return this;
        }
    }
}
