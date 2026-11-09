package com.fiveoutoffive.moa_backend.file.application.usecase;

import com.fiveoutoffive.moa_backend.file.application.result.DownloadUrlResponse;
import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.file.application.result.RoomFileResponse;
import com.fiveoutoffive.moa_backend.file.application.result.StorageUsageResponse;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FileUseCase {

    FileResponse upload(Long accountId, Long roomId, MultipartFile file, FileGroup purpose);

    void cancelUpload(Long accountId, Long roomId, Long fileId);

    PageResponse<RoomFileResponse> getRoomFiles(Long accountId, Long roomId, String source, String fileType, Long uploaderId, String keyword, Pageable pageable);

    DownloadUrlResponse getDownloadUrl(Long accountId, Long roomId, Long fileId);

    StorageUsageResponse getStorageUsage(Long accountId, Long roomId);
}
