package com.fiveoutoffive.moa_backend.room.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.room.application.result.RoomDetailResponse;
import com.fiveoutoffive.moa_backend.room.application.result.RoomSummaryResponse;
import com.fiveoutoffive.moa_backend.room.application.usecase.RoomUseCase;
import com.fiveoutoffive.moa_backend.room.presentation.api.request.RoomCreateRequest;
import com.fiveoutoffive.moa_backend.room.presentation.api.request.RoomDeleteRequest;
import com.fiveoutoffive.moa_backend.room.presentation.api.request.RoomUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "04. Room", description = "방")
public class RoomController {

    private final RoomUseCase roomUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "방 개설")
    public ApiResponse<RoomDetailResponse> create(@LoginAccount Long accountId, @Valid @RequestPart("request") RoomCreateRequest request,
                                                  @RequestPart(value = "image", required = false) MultipartFile image) {
        return ApiResponse.success(roomUseCase.create(accountId, request.toCommand(), image));
    }

    @GetMapping
    @Operation(summary = "내 방 목록")
    public ApiResponse<List<RoomSummaryResponse>> getMyRooms(@LoginAccount Long accountId) {
        return ApiResponse.success(roomUseCase.getMyRooms(accountId));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "방 정보 조회 (방 진입)")
    public ApiResponse<RoomDetailResponse> getRoom(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(roomUseCase.getRoom(accountId, roomId));
    }

    @PatchMapping("/{roomId}")
    @Operation(summary = "방 설정 변경")
    public ApiResponse<RoomDetailResponse> update(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody RoomUpdateRequest request) {
        return ApiResponse.success(roomUseCase.update(accountId, roomId, request.toCommand()));
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "방 삭제")
    public ApiResponse<Void> delete(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody RoomDeleteRequest request) {
        roomUseCase.delete(accountId, roomId, request.toCommand());
        return ApiResponse.empty();
    }
}
