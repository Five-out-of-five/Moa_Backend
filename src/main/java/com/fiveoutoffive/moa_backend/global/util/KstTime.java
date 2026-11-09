package com.fiveoutoffive.moa_backend.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * 시각 변환 유틸 (API 명세 공통 규칙 "날짜·시간").
 *
 * <ul>
 *   <li>DB 의 TIMESTAMP(6) 는 타임존 없이 UTC 로 저장한다. 엔티티의 LocalDateTime 은 UTC 값이다.</li>
 *   <li>응답은 한국 시간 ISO-8601 문자열(예: 2026-10-28T09:00:00+09:00)로 내보낸다 → {@link #toKst}</li>
 *   <li>요청 시각은 오프셋을 붙여 받는다(OffsetDateTime) → {@link #toUtc} 로 바꿔 저장한다.</li>
 *   <li>DATE 컬럼(상태값 기록일, AI 사용량 일자 등)은 한국 날짜다 → {@link #todayKst}</li>
 * </ul>
 */
public final class KstTime {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private KstTime() {
    }

    /** 저장할 현재 시각 (UTC). JVM 기본 타임존과 상관없이 같은 값이 나온다. */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /** 오늘 날짜 (한국 기준). DATE 컬럼에 쓴다. */
    public static LocalDate todayKst() {
        return LocalDate.now(SEOUL);
    }

    /** 한국 날짜의 시작 시각(00:00 KST)을 UTC 로 바꾼다. 일자 경계 집계에 쓴다. */
    public static LocalDateTime startOfDayUtc(LocalDate date) {
        return date.atStartOfDay(SEOUL).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    /** DB 에서 읽은 UTC 시각을 응답용 한국 시각(+09:00)으로 바꾼다. null 이면 null. */
    public static OffsetDateTime toKst(LocalDateTime utc) {
        if (utc == null) {
            return null;
        }
        return utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(SEOUL).toOffsetDateTime();
    }

    /** 요청으로 받은 오프셋 시각을 저장용 UTC 시각으로 바꾼다. null 이면 null. */
    public static LocalDateTime toUtc(OffsetDateTime time) {
        if (time == null) {
            return null;
        }
        return time.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
