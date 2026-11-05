package com.fiveoutoffive.moa_backend.account.domain.model;

/** 가입한 소셜 제공자 (계정당 1개, 변경·해제 불가. GITHUB 은 추후 확장) (account.provider) */
public enum SocialProvider {
    GOOGLE,
    KAKAO,
    NAVER
}
