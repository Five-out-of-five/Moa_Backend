package com.fiveoutoffive.moa_backend.room.presentation.api.request;

import com.fiveoutoffive.moa_backend.room.application.command.MemberProfileUpdateCommand;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Optional;

/**
 * 방별 내 프로필 설정. 보낸 필드만 바꾼다(PATCH).
 *
 * <p>각 필드는 "보내지 않음(그대로 둠)"과 "null 을 보냄(지움)"을 구분해야 한다.
 * 응답에 프로필 이미지 파일 ID 가 없어서, 통째로 교체하면 닉네임만 고쳐도 이미지가 지워졌다.
 * {@link RoomUpdateRequest} 와 같이 setter 가 불렸는지(= JSON 에 키가 있었는지)를 따로 기록한다.
 */
public class MemberProfileUpdateRequest {

    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[\\p{L}\\p{N} ]+$", message = "특수문자는 사용할 수 없습니다.")
    private String nickname;
    private Long profileFileId;
    @Size(max = 20)
    private String positionLabel;

    private boolean nicknameSent;
    private boolean profileFileIdSent;
    private boolean positionLabelSent;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameSent = true;
    }

    public Long getProfileFileId() {
        return profileFileId;
    }

    public void setProfileFileId(Long profileFileId) {
        this.profileFileId = profileFileId;
        this.profileFileIdSent = true;
    }

    public String getPositionLabel() {
        return positionLabel;
    }

    public void setPositionLabel(String positionLabel) {
        this.positionLabel = positionLabel;
        this.positionLabelSent = true;
    }

    /** 각 필드: null = 보내지 않음(유지), Optional.empty() = 지움, 값 = 변경. */
    public MemberProfileUpdateCommand toCommand() {
        return new MemberProfileUpdateCommand(
                nicknameSent ? Optional.ofNullable(nickname) : null,
                profileFileIdSent ? Optional.ofNullable(profileFileId) : null,
                positionLabelSent ? Optional.ofNullable(positionLabel) : null);
    }
}
