package com.fiveoutoffive.moa_backend.file.application.service;

import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.file.exception.FileErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.port.out.FileStoragePort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 업로드한 파일을 메시지·카드·업무·프로필에 연결할 때의 공통 검증과 응답 변환. */
@Component
@RequiredArgsConstructor
public class FileAttachSupport {

    public static final int MAX_ATTACHMENTS = 5;
    private static final Duration PREVIEW_TTL = Duration.ofHours(1);
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final StoredFileRepository storedFileRepository;
    private final FileStoragePort fileStoragePort;

    public void requireOwnedOfGroup(Long roomId, Long accountId, Long fileId, FileGroup group) {
        StoredFile file = find(roomId, fileId);
        if (!file.getOwnerAccountId().equals(accountId)) {
            throw new BusinessException(FileErrorCode.FILE_NOT_OWNER);
        }
        if (file.getFileGroup() != group) {
            throw new BusinessException(FileErrorCode.FILE_PURPOSE_MISMATCH);
        }
    }

    /** 새로 연결할 파일들을 검증한다. currentCount 는 이미 연결된 개수다. */
    public void requireAttachable(Long roomId, Long accountId, Collection<Long> fileIds, FileGroup group, int currentCount) {
        if (currentCount + fileIds.size() > MAX_ATTACHMENTS) {
            throw new BusinessException(FileErrorCode.FILE_ATTACHMENT_LIMIT_EXCEEDED);
        }
        for (Long fileId : fileIds) {
            requireOwnedOfGroup(roomId, accountId, fileId, group);
            if (storedFileRepository.isAttached(fileId)) {
                throw new BusinessException(FileErrorCode.FILE_ALREADY_ATTACHED);
            }
        }
    }

    public StoredFile find(Long roomId, Long fileId) {
        return storedFileRepository.findById(fileId)
                .filter(file -> file.getDeletedAt() == null && file.getRoomId().equals(roomId))
                .orElseThrow(() -> new BusinessException(FileErrorCode.FILE_NOT_FOUND));
    }

    public void softDelete(Collection<Long> fileIds) {
        LocalDateTime now = KstTime.nowUtc();
        storedFileRepository.findByIdIn(fileIds).forEach(file -> storedFileRepository.save(file.toBuilder().deletedAt(now).build()));
    }

    public List<FileResponse> responses(Collection<Long> fileIds) {
        if (fileIds.isEmpty()) {
            return List.of();
        }
        Map<Long, StoredFile> files = storedFileRepository.findByIdIn(fileIds).stream()
                .filter(file -> file.getDeletedAt() == null)
                .collect(Collectors.toMap(StoredFile::getId, Function.identity()));
        return fileIds.stream().map(files::get).filter(Objects::nonNull).map(this::response).toList();
    }

    public FileResponse response(StoredFile file) {
        String extension = file.getOriginalName().contains(".") ? file.getOriginalName().substring(file.getOriginalName().lastIndexOf('.') + 1).toLowerCase() : "";
        String preview = IMAGE_EXTENSIONS.contains(extension) ? fileStoragePort.createDownloadUrl(file.getObjectKey(), null, PREVIEW_TTL) : null;
        return new FileResponse(file.getId(), file.getOriginalName(), file.getMimeType(), file.getSizeBytes(), file.getFileGroup(), preview);
    }
}
