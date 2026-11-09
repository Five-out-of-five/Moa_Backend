package com.fiveoutoffive.moa_backend.global.port.out;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Python AI 서버 호출 포트. 어떤 실패도 예외로 전파하지 않는다.
 * 서버가 꺼져 있거나 응답이 잘못되면 빈 값을 돌려주며, 처리 방식(기본값, AI_UNAVAILABLE 등)은 호출한 쪽이 정한다.
 * 호출 계약은 docs/ai-server.md 를 따른다.
 */
public interface AiServerPort {

    boolean isAvailable();

    /** JSON 요청 → JSON 응답. 실패하면 Optional.empty() */
    <T> Optional<T> post(String path, Object body, Class<T> responseType);

    /**
     * SSE 스트리밍 호출. 줄 단위 data 값을 onDelta 로 넘긴다.
     *
     * @return 스트림을 끝까지 받았으면 true, 연결·중단 등으로 실패했으면 false
     */
    boolean stream(String path, Object body, Consumer<String> onDelta);
}
