package com.fiveoutoffive.moa_backend.admin.presentation.api;

import com.fiveoutoffive.moa_backend.admin.application.result.AdminRoomDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminRoomResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminRoomUseCase;
import com.fiveoutoffive.moa_backend.admin.presentation.api.request.RoomInactivateRequest;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/rooms")
@RequiredArgsConstructor
@Tag(name = "19. Admin", description = "서비스 관리자")
public class AdminRoomController {

    private final AdminRoomUseCase useCase;

    @GetMapping
    @Operation(summary = "[관리자] 방 목록")
    public ApiResponse<PageResponse<AdminRoomResponse>> list(@RequestParam(required = false) String keyword, @RequestParam(required = false) RoomStatus status, @RequestParam(required = false) Integer inactiveDays, @RequestParam(defaultValue = "CREATED") String sort, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Sort order = Sort.by(Sort.Direction.DESC, "LAST_ACTIVITY".equals(sort) ? "lastActivityAt" : "createdAt");
        return ApiResponse.success(useCase.getRooms(keyword, status, inactiveDays, Pages.of(page, size, order)));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "[관리자] 방 상세")
    public ApiResponse<AdminRoomDetailResponse> detail(@PathVariable Long roomId) {
        return ApiResponse.success(useCase.getRoom(roomId));
    }

    @PostMapping("/{roomId}/inactivation")
    @Operation(summary = "[관리자] 방 비활성화")
    public ApiResponse<AdminRoomDetailResponse> inactivate(@LoginAccount Long adminId, @PathVariable Long roomId, @Valid @RequestBody RoomInactivateRequest request) {
        return ApiResponse.success(useCase.inactivate(adminId, roomId, request.toCommand()));
    }

    @DeleteMapping("/{roomId}/inactivation")
    @Operation(summary = "[관리자] 방 비활성 해제")
    public ApiResponse<AdminRoomDetailResponse> reactivate(@LoginAccount Long adminId, @PathVariable Long roomId) {
        return ApiResponse.success(useCase.reactivate(adminId, roomId));
    }
}
