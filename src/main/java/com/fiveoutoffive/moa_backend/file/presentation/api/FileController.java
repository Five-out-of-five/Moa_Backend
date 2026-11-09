package com.fiveoutoffive.moa_backend.file.presentation.api;

import com.fiveoutoffive.moa_backend.file.application.result.DownloadUrlResponse;
import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.file.application.result.RoomFileResponse;
import com.fiveoutoffive.moa_backend.file.application.result.StorageUsageResponse;
import com.fiveoutoffive.moa_backend.file.application.usecase.FileUseCase;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}")
@RequiredArgsConstructor
@Tag(name = "03. File", description = "파일")
public class FileController {

    private final FileUseCase fileUseCase;

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드")
    public ApiResponse<FileResponse> upload(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestPart("file") MultipartFile file, @RequestParam FileGroup purpose) {
        return ApiResponse.success(fileUseCase.upload(accountId, roomId, file, purpose));
    }

    @DeleteMapping("/files/{fileId}")
    @Operation(summary = "업로드 취소")
    public ApiResponse<Void> cancel(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long fileId) {
        fileUseCase.cancelUpload(accountId, roomId, fileId);
        return ApiResponse.empty();
    }

    @GetMapping("/files")
    @Operation(summary = "방 파일 목록")
    public ApiResponse<PageResponse<RoomFileResponse>> list(@LoginAccount Long accountId, @PathVariable Long roomId,
                                                            @RequestParam(required = false) String source, @RequestParam(required = false) String fileType,
                                                            @RequestParam(required = false) Long uploaderId, @RequestParam(required = false) String keyword,
                                                            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(fileUseCase.getRoomFiles(accountId, roomId, source, fileType, uploaderId, keyword, Pages.of(page, size)));
    }

    @GetMapping("/files/{fileId}/download-url")
    @Operation(summary = "다운로드 URL 발급")
    public ApiResponse<DownloadUrlResponse> downloadUrl(@LoginAccount Long accountId, @PathVariable Long roomId, @PathVariable Long fileId) {
        return ApiResponse.success(fileUseCase.getDownloadUrl(accountId, roomId, fileId));
    }

    @GetMapping("/storage-usage")
    @Operation(summary = "방 저장 용량 조회")
    public ApiResponse<StorageUsageResponse> storageUsage(@LoginAccount Long accountId, @PathVariable Long roomId) {
        return ApiResponse.success(fileUseCase.getStorageUsage(accountId, roomId));
    }
}
