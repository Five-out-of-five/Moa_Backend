package com.fiveoutoffive.moa_backend.admin.application.usecase;

import com.fiveoutoffive.moa_backend.admin.application.command.RoomInactivateCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminRoomDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminRoomResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import org.springframework.data.domain.Pageable;

public interface AdminRoomUseCase {

    /** orderByReportCount 가 true 면 신고 수 많은 순, 아니면 pageable 의 정렬을 쓴다. */
    PageResponse<AdminRoomResponse> getRooms(String keyword, RoomStatus status, Integer inactiveDays, boolean orderByReportCount, Pageable pageable);

    AdminRoomDetailResponse getRoom(Long roomId);

    AdminRoomDetailResponse inactivate(Long adminId, Long roomId, RoomInactivateCommand command);

    AdminRoomDetailResponse reactivate(Long adminId, Long roomId);
}
