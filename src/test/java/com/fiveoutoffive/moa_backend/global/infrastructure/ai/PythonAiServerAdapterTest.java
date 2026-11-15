package com.fiveoutoffive.moa_backend.global.infrastructure.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 어시스턴트 스트림이 도중에 끊겨도 AI 서버 전체를 '끊김'으로 표시하지 않는지 본다.
 * Python 은 답변 도중 LLM 이 실패하면 일부러 연결을 비정상 종료한다. 이를 소켓 서버로 그대로 흉내 낸다.
 */
@DisplayName("AI 서버 어댑터 (스트림 끊김)")
class PythonAiServerAdapterTest {

    private static final String DELTA = "{\"delta\":\"앞부분\"}";

    private ServerSocket server;
    private volatile boolean dropMidStream;
    private AiServerConnection connection;
    private PythonAiServerAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        server = new ServerSocket(0);
        Thread.ofVirtual().start(this::serve);
        connection = new AiServerConnection("http://localhost:" + server.getLocalPort(), Duration.ofSeconds(2), Duration.ofSeconds(5), "");
        adapter = new PythonAiServerAdapter(connection, JsonMapper.builder().build());
        connection.checkHealth();
        assertThat(connection.isConnected()).isTrue();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
    }

    @Test
    @DisplayName("답변 도중 연결이 끊기면 실패(false)로 끝나지만 서버는 연결된 상태로 남는다")
    void midStreamDropKeepsConnection() {
        dropMidStream = true;
        List<String> lines = new CopyOnWriteArrayList<>();

        boolean completed = adapter.stream("/v1/assistant/stream", Map.of("question", "q"), lines::add);

        assertThat(completed).isFalse();
        assertThat(lines).containsExactly(DELTA);
        assertThat(connection.isConnected()).isTrue();
    }

    @Test
    @DisplayName("끝까지 받으면 성공(true)")
    void completeStream() {
        dropMidStream = false;
        List<String> lines = new CopyOnWriteArrayList<>();

        boolean completed = adapter.stream("/v1/assistant/stream", Map.of("question", "q"), lines::add);

        assertThat(completed).isTrue();
        assertThat(lines).containsExactly(DELTA, "{\"references\":{}}");
        assertThat(connection.isConnected()).isTrue();
    }

    @Test
    @DisplayName("연결 자체가 안 되면 지금처럼 끊김으로 표시한다")
    void connectionFailureMarksDisconnected() throws IOException {
        server.close();

        boolean completed = adapter.stream("/v1/assistant/stream", Map.of("question", "q"), line -> { });

        assertThat(completed).isFalse();
        assertThat(connection.isConnected()).isFalse();
    }

    private void serve() {
        while (!server.isClosed()) {
            try (Socket socket = server.accept()) {
                handle(socket);
            } catch (IOException e) {
                // 테스트가 끝나 서버를 닫았다.
            }
        }
    }

    private void handle(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
        String requestLine = reader.readLine();
        int contentLength = 0;
        String header;
        while ((header = reader.readLine()) != null && !header.isEmpty()) {
            if (header.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(header.substring(15).trim());
            }
        }
        reader.skip(contentLength);
        OutputStream out = socket.getOutputStream();
        if (requestLine == null || requestLine.contains("/health")) {
            out.write("HTTP/1.1 200 OK\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
            return;
        }
        out.write("HTTP/1.1 200 OK\r\nContent-Type: text/event-stream\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        writeChunk(out, "data: " + DELTA + "\n\n");
        if (dropMidStream) {
            // 마지막 0 청크 없이 소켓을 닫는다. 클라이언트는 본문을 읽다가 예외를 받는다.
            out.flush();
            return;
        }
        writeChunk(out, "data: {\"references\":{}}\n\n");
        out.write("0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    private static void writeChunk(OutputStream out, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        out.write((Integer.toHexString(bytes.length) + "\r\n").getBytes(StandardCharsets.US_ASCII));
        out.write(bytes);
        out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }
}
