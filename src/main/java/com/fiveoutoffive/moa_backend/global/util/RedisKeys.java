package com.fiveoutoffive.moa_backend.global.util;

/**
 * Redis 키 접두사를 한 곳에 모아둔다.
 *
 * <p>여러 클래스에 문자열 리터럴로 흩어지면 오타로 read/write 키가 어긋나도 컴파일 에러가 나지 않는다.
 * 새 키를 추가할 때는 반드시 여기에 상수로 선언하고, 값은 {@code "PREFIX:"} 형태로 콜론을 붙인다.
 */
public final class RedisKeys {

    /** 리프레시 토큰 저장 (RT:{subject}) */
    public static final String REFRESH_TOKEN_PREFIX = "RT:";

    /** 이메일/휴대폰 인증코드 저장 (AUTH_CODE:{target}) */
    public static final String AUTH_CODE_PREFIX = "AUTH_CODE:";

    /** 인증 성공 마커 (AUTH_SUCCESS:{target}) */
    public static final String AUTH_SUCCESS_PREFIX = "AUTH_SUCCESS:";

    /** WebSocket 접속 중인 사용자 마커 (ONLINE:{userId}) */
    public static final String ONLINE_PREFIX = "ONLINE:";

    /** 가입 티켓 (SIGNUP_TICKET:{ticket}) */
    public static final String SIGNUP_TICKET_PREFIX = "SIGNUP_TICKET:";

    /** 리프레시 토큰 교체 동시 요청 락 (RT_LOCK:{accountId}) */
    public static final String REFRESH_LOCK_PREFIX = "RT_LOCK:";

    /** 교체 직후 유예 시간 동안 같은 결과를 돌려주기 위한 값 (RT_GRACE:{이전 토큰 해시}) */
    public static final String REFRESH_GRACE_PREFIX = "RT_GRACE:";

    /** 정지 회원 표시 (SUSPENDED:{accountId}) */
    public static final String SUSPENDED_PREFIX = "SUSPENDED:";

    /** 오늘 접속 기록 여부 (ACTIVE:{yyyy-MM-dd}:{accountId}) */
    public static final String DAILY_ACTIVE_PREFIX = "ACTIVE:";

    /** 카드 편집 잠금 (CARD_LOCK:{cardId}) */
    public static final String CARD_LOCK_PREFIX = "CARD_LOCK:";

    /** 메시지 재전송 중복 방지 (MSG_DEDUP:{channelId}:{clientMessageId}) */
    public static final String MESSAGE_DEDUP_PREFIX = "MSG_DEDUP:";

    /** 노션 OAuth state (NOTION_STATE:{state}) */
    public static final String NOTION_STATE_PREFIX = "NOTION_STATE:";

    private RedisKeys() {
        throw new IllegalStateException("Utility class");
    }
}
