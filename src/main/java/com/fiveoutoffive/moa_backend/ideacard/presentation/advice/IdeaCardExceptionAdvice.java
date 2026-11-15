package com.fiveoutoffive.moa_backend.ideacard.presentation.advice;

import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.exception.CommonExceptionAdvice;
import com.fiveoutoffive.moa_backend.global.common.api.response.ErrorResponse;
import com.fiveoutoffive.moa_backend.ideacard.exception.IdeaCardErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// 카드 API 에서는 전역 핸들러보다 먼저 본다. 순서가 없으면 전역 핸들러가 먼저 잡아 아래 낙관적 락 처리가 쓰이지 않는다.
// 나머지 예외 처리는 CommonExceptionAdvice 를 그대로 물려받아 전역과 같다.
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.fiveoutoffive.moa_backend.ideacard.presentation.api")
public class IdeaCardExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }

    /**
     * 여러 명이 같은 칸의 카드를 동시에 옮기거나, 수정과 이동이 겹치면 카드 version 충돌이 커밋 때 난다.
     * 500 대신 명세의 409 CARD_VERSION_CONFLICT 로 답한다. 프론트는 카드 목록을 다시 읽는다.
     * 잠금 대기 실패(교착 등)도 같은 원인이라 같이 409 로 본다.
     */
    @ExceptionHandler({OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(RuntimeException e) {
        return handleBusinessException(new BusinessException(IdeaCardErrorCode.CARD_VERSION_CONFLICT));
    }
}
