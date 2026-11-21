package com.fiveoutoffive.moa_backend.mood.domain.model;

/** 팀 상태값 현황의 추이 기간 (API 명세: TODAY / DAYS_7 / DAYS_30). 다른 값은 400 이다. */
public enum MoodPeriod {
    TODAY(0),
    DAYS_7(7),
    DAYS_30(30);

    private final int days;

    MoodPeriod(int days) {
        this.days = days;
    }

    /** 추이에 담을 날 수. 0 이면 추이를 만들지 않는다. */
    public int days() {
        return days;
    }
}
