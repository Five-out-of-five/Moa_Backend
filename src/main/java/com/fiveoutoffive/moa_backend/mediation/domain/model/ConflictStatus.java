package com.fiveoutoffive.moa_backend.mediation.domain.model;

/** 감지됨 / 해제됨 / 절충안 제안됨 / 절충안 폐기 후 재감지 (card_conflict.status) */
public enum ConflictStatus {
    DETECTED,
    DISMISSED,
    PROPOSED,
    REDETECTED
}
