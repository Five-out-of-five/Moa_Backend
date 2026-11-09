package com.fiveoutoffive.moa_backend.admin.application.usecase;

import com.fiveoutoffive.moa_backend.admin.application.command.RoomInactivateCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminRoomDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminRoomResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import org.springframework.data.domain.Pageable;

public interface AdminRoomUseCase {

    PageResponse<AdminRoomResponse> getRooms(String keyword, RoomStatus status, Integer inactiveDays, Pageable pageable);

    AdminRoomDetailResponse getRoom(Long roomId);

    AdminRoomDetailResponse inactivate(Long adminId, Long roomId, RoomInactivateCommand command);

    AdminRoomDetailResponse reactivate(Long adminId, Long roomId);
}
