package com.fiveoutoffive.moa_backend.global.infrastructure.ai;

import com.fiveoutoffive.moa_backend.global.port.out.AiServerPort;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonAiServerAdapter implements AiServerPort {

    private static final String SSE_DATA_PREFIX = "data:";

    private final AiServerConnection connection;
    private final JsonMapper jsonMapper;

    @Override
    public boolean isAvailable() {
        return connection.isConnected();
    }

    @Override
    public <T> Optional<T> post(String path, Object body, Class<T> responseType) {
        if (!connection.isConnected()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(connection.client().post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJson(body))
                    .retrieve()
                    .body(responseType));
        } catch (Exception e) {
            onFailure(path, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean stream(String path, Object body, Consumer<String> onDelta) {
        if (!connection.isConnected()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(connection.client().post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .body(toJson(body))
                    .exchange((request, response) -> {
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            return false;
                        }
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith(SSE_DATA_PREFIX)) {
                                    onDelta.accept(line.substring(SSE_DATA_PREFIX.length()).stripLeading());
                                }
                            }
                        }
                        return true;
                    }));
        } catch (Exception e) {
            onFailure(path, e);
            return false;
        }
    }

    /** 바이트로 보내야 Content-Length 가 붙는다. 객체를 그대로 넘기면 chunked 로 나가 keep-alive 연결이 서버 구현에 좌우된다. */
    private byte[] toJson(Object body) {
        return jsonMapper.writeValueAsBytes(body);
    }

    private void onFailure(String path, Exception e) {
        log.warn("[AI] 호출 실패 path={}: {}", path, e.getMessage());
        // 4xx·5xx 는 서버가 살아 있다는 뜻이다. 연결 자체가 실패한 경우에만 끊김으로 표시한다.
        if (!(e instanceof org.springframework.web.client.RestClientResponseException)) {
            connection.markDisconnected(e.getMessage());
        }
    }
}
