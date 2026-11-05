package com.fiveoutoffive.moa_backend.global.common.api.response;

import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnMappable;
import com.fiveoutoffive.moa_backend.global.type.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 작성자·담당자·발신자 등 사람을 가리키는 응답 필드의 공통 형식 (API 명세 공통 규칙 "표시 이름 (MemberRef)").
 *
 * <p>displayName 은 방별 닉네임, 없으면 기본 프로필 이름이다.
 * profileImageUrl 에는 object key 를 넣는다. CdnMappable 이라 다른 응답 DTO 안에 들어가도 직렬화 시점에 절대 URL 로 바뀐다.
 *
 * <p>LEFT(방을 나간 사용자), WITHDRAWN(탈퇴한 유저)은 이름·이미지를 비워서 내려준다(명세 규칙).
 * 화면 문구는 프론트가 memberStatus 를 보고 표시한다.
 */
@Schema(description = "사람 표시 정보")
public record MemberRef(
        @Schema(description = "계정 ID", example = "12")
        Long accountId,

        @Schema(description = "표시 이름 (방별 닉네임, 없으면 기본 이름). LEFT·WITHDRAWN 이면 null", example = "모아짱")
        String displayName,

        @Schema(description = "방별 프로필 이미지 URL. 미등록·LEFT·WITHDRAWN 이면 null",
                example = "https://cdn.example.com/rooms/1/profiles/uuid.png")
        String profileImageUrl,

        @Schema(description = "멤버 상태", example = "ACTIVE")
        MemberStatus memberStatus
) implements CdnMappable {

    /**
     * 상태에 맞춰 이름·이미지를 정리한 MemberRef 를 만든다.
     *
     * @param profileImageKey 프로필 이미지의 object key (절대 URL 아님)
     */
    public static MemberRef of(Long accountId, String displayName, String profileImageKey, MemberStatus memberStatus) {
        if (memberStatus == MemberStatus.LEFT || memberStatus == MemberStatus.WITHDRAWN) {
            return new MemberRef(accountId, null, null, memberStatus);
        }
        return new MemberRef(accountId, displayName, profileImageKey, memberStatus);
    }
}
