package com.fiveoutoffive.moa_backend.admin.application.result;

import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import java.time.LocalDateTime;

public record AdminRoomResponse(Long roomId, String name, Person creator, long memberCount, RoomStatus status, LocalDateTime lastActivityAt, long reportCount, LocalDateTime createdAt) {

    public record Person(Long accountId, String name) {
    }
}
