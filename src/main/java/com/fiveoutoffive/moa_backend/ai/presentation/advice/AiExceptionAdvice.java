package com.fiveoutoffive.moa_backend.ai.presentation.advice;

import com.fiveoutoffive.moa_backend.global.exception.CommonExceptionAdvice;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.fiveoutoffive.moa_backend.ai.presentation.api")
public class AiExceptionAdvice implements CommonExceptionAdvice {

    @Override
    public Logger getLogger() {
        return log;
    }
}
