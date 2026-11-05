package com.fiveoutoffive.moa_backend.global.common.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.function.Function;

/**
 * 커서 기반 목록 응답 (API 명세 공통 규칙 "페이지네이션": 채팅 메시지, AI 대화 이력).
 * 커서는 마지막 항목의 id 이고, 다음 요청에서 before(또는 after) 값으로 그대로 보낸다.
 */
@Schema(description = "커서 기반 목록 응답")
public record CursorResponse<T>(
        @Schema(description = "목록")
        List<T> items,

        @Schema(description = "다음 목록이 더 있는지", example = "true")
        boolean hasNext,

        @Schema(description = "다음 요청에 보낼 커서(마지막 항목 id). 더 없으면 null", example = "1024")
        Long nextCursor
) {

    /**
     * size + 1 개를 조회한 결과로 응답을 만든다.
     * 하나 더 조회되면 다음 목록이 있다는 뜻이므로, 넘친 1개는 버리고 hasNext = true 로 둔다.
     *
     * @param fetched     size + 1 개까지 조회한 목록 (정렬된 상태)
     * @param size        요청한 개수
     * @param idExtractor 항목에서 커서로 쓸 id 를 꺼내는 함수
     */
    public static <T> CursorResponse<T> of(List<T> fetched, int size, Function<T, Long> idExtractor) {
        boolean hasNext = fetched.size() > size;
        List<T> items = hasNext ? List.copyOf(fetched.subList(0, size)) : List.copyOf(fetched);
        Long nextCursor = hasNext && !items.isEmpty() ? idExtractor.apply(items.get(items.size() - 1)) : null;
        return new CursorResponse<>(items, hasNext, nextCursor);
    }
}
