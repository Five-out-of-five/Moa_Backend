package com.fiveoutoffive.moa_backend.global.util;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** 목록 공통 규칙: page(0부터), size(기본 20, 최대 100) */
public final class Pages {

    private static final int MAX_SIZE = 100;

    private Pages() {
    }

    public static Pageable of(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_SIZE));
    }

    public static Pageable of(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_SIZE), sort);
    }

    public static <T> PageResponse<T> response(Page<T> page) {
        return PageResponse.from(page);
    }
}
