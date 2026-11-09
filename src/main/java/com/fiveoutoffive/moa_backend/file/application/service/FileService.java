package com.fiveoutoffive.moa_backend.file.application.service;

import com.fiveoutoffive.moa_backend.file.application.result.DownloadUrlResponse;
import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.file.application.result.RoomFileResponse;
import com.fiveoutoffive.moa_backend.file.application.result.StorageUsageResponse;
import com.fiveoutoffive.moa_backend.file.application.usecase.FileUseCase;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.file.exception.FileErrorCode;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.port.out.FileStoragePort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService implements FileUseCase {

    private static final Duration DOWNLOAD_TTL = Duration.ofMinutes(10);
    private static final Map<String, String> SOURCE_GROUPS = Map.of("CHAT", "CHAT_ATTACHMENT", "IDEA_CARD", "CARD_ATTACHMENT", "TASK", "TASK_ATTACHMENT");
    private static final Map<String, String> GROUP_SOURCES = Map.of("CHAT_ATTACHMENT", "CHAT", "CARD_ATTACHMENT", "IDEA_CARD", "TASK_ATTACHMENT", "TASK");
    private static final Map<String, String> FILE_TYPE_MIME = Map.of("IMAGE", "image/%", "DOCUMENT", "application/%", "ARCHIVE", "application/zip");

    private final StoredFileRepository storedFileRepository;
    private final FileUploadSupport uploadSupport;
    private final FileAttachSupport attachSupport;
    private final FileStoragePort fileStoragePort;
    private final RoomAccessService access;

    @Override
    public FileResponse upload(Long accountId, Long roomId, MultipartFile file, FileGroup purpose) {
        if (purpose == FileGroup.ROOM_IMAGE) {
            access.requireOwner(roomId, accountId);
        } else {
            access.requireMember(roomId, accountId);
        }
        return attachSupport.response(uploadSupport.upload(roomId, accountId, file, purpose));
    }

    @Override
    public void cancelUpload(Long accountId, Long roomId, Long fileId) {
        access.requireMember(roomId, accountId);
        StoredFile file = attachSupport.find(roomId, fileId);
        if (!file.getOwnerAccountId().equals(accountId)) {
            throw new BusinessException(FileErrorCode.FILE_NOT_OWNER);
        }
        if (storedFileRepository.isAttached(fileId)) {
            throw new BusinessException(FileErrorCode.FILE_ALREADY_ATTACHED);
        }
        fileStoragePort.deleteFile(file.getObjectKey());
        storedFileRepository.deleteById(fileId);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<RoomFileResponse> getRoomFiles(Long accountId, Long roomId, String source, String fileType, Long uploaderId, String keyword, Pageable pageable) {
        access.requireMember(roomId, accountId);
        Page<Object[]> rows = storedFileRepository.searchRoomFiles(roomId, source == null ? null : SOURCE_GROUPS.get(source), uploaderId, keyword,
                fileType == null ? null : FILE_TYPE_MIME.get(fileType), pageable);
        Set<Long> uploaderIds = rows.stream().map(row -> ((Number) row[4]).longValue()).collect(Collectors.toSet());
        Map<Long, MemberRef> refs = access.memberRefs(roomId, uploaderIds);
        return Pages.response(rows.map(row -> new RoomFileResponse(((Number) row[0]).longValue(), (String) row[1], (String) row[2], ((Number) row[3]).longValue(),
                refs.get(((Number) row[4]).longValue()), GROUP_SOURCES.get((String) row[6]), row[7] == null ? null : ((Number) row[7]).longValue(),
                row[5] instanceof java.sql.Timestamp timestamp ? timestamp.toLocalDateTime() : (LocalDateTime) row[5])));
    }

    @Transactional(readOnly = true)
    @Override
    public DownloadUrlResponse getDownloadUrl(Long accountId, Long roomId, Long fileId) {
        access.requireMember(roomId, accountId);
        StoredFile file = attachSupport.find(roomId, fileId);
        return new DownloadUrlResponse(fileStoragePort.createDownloadUrl(file.getObjectKey(), file.getOriginalName(), DOWNLOAD_TTL), KstTime.nowUtc().plus(DOWNLOAD_TTL));
    }

    @Transactional(readOnly = true)
    @Override
    public StorageUsageResponse getStorageUsage(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        return new StorageUsageResponse(storedFileRepository.sumSizeByRoomId(roomId), FileUploadSupport.ROOM_STORAGE_LIMIT);
    }
}
