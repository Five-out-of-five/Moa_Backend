package com.fiveoutoffive.moa_backend.admin.application.result;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.LocalDateTime;

public record AdminRoomDetailResponse(@JsonUnwrapped AdminRoomResponse room, String description, String inactiveReason, LocalDateTime inactivatedAt, AdminRoomResponse.Person inactivatedBy) {
}
