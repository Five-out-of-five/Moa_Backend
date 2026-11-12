package com.fiveoutoffive.moa_backend.room.presentation.api.request;

import com.fiveoutoffive.moa_backend.room.application.command.RoomUpdateCommand;
import jakarta.validation.constraints.Size;
import java.util.Optional;

/**
 * 방 설정 변경. 보낸 필드만 바꾼다(API 명세 '방 설정 변경').
 *
 * <p>imageFileId 는 "보내지 않음(그대로 둠)"과 "null 을 보냄(이미지 삭제)"을 구분해야 한다.
 * record 의 {@code Optional<Long>} 은 두 경우 모두 {@code Optional.empty()} 로 들어와 이름만 바꿔도 이미지가 지워졌다.
 * 그래서 setter 가 불렸는지(= JSON 에 키가 있었는지)를 따로 기록한다. Jackson 은 값이 null 이어도 키가 있으면 setter 를 부른다.
 */
public class RoomUpdateRequest {

    @Size(min = 2, max = 30)
    private String name;

    @Size(max = 200)
    private String description;

    private Long imageFileId;

    private boolean imageFileIdSent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getImageFileId() {
        return imageFileId;
    }

    public void setImageFileId(Long imageFileId) {
        this.imageFileId = imageFileId;
        this.imageFileIdSent = true;
    }

    /** imageFileId: null = 보내지 않음(유지), Optional.empty() = 이미지 삭제, 값 = 새 이미지. */
    public RoomUpdateCommand toCommand() {
        return new RoomUpdateCommand(name, description, imageFileIdSent ? Optional.ofNullable(imageFileId) : null);
    }
}
