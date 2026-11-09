package com.fiveoutoffive.moa_backend.global.port.out;

/** 인증된 요청마다 호출된다. 정지 회원은 BusinessException(AUTH_ACCOUNT_SUSPENDED)을 던지고, 일별 접속을 기록한다. */
public interface AccountGuardPort {

    void onAuthenticatedRequest(Long accountId);
}
