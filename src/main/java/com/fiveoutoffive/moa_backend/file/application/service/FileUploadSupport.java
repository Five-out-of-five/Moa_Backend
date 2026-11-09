package com.fiveoutoffive.moa_backend.file.application.service;

import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.file.exception.FileErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.port.out.FileStoragePort;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** 업로드 검증과 저장. 방 개설처럼 트랜잭션 안에서 다른 도메인도 호출한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadSupport {

    public static final long ROOM_STORAGE_LIMIT = 2L * 1024 * 1024 * 1024;
    private static final long IMAGE_LIMIT = 5L * 1024 * 1024;
    private static final long ATTACHMENT_LIMIT = 20L * 1024 * 1024;
    private static final Set<String> PROFILE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ATTACHMENT_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "docx", "xlsx", "pptx", "txt", "md", "zip");
    /** 프로필·방 대표 이미지만 공개 경로에 둔다. 첨부는 서명 URL 로만 받는다. */
    private static final String PUBLIC_DIR = "public/rooms/%d/%s";
    private static final String PRIVATE_DIR = "rooms/%d/%s";

    private final StoredFileRepository storedFileRepository;
    private final FileStoragePort fileStoragePort;

    public StoredFile upload(Long roomId, Long accountId, MultipartFile file, FileGroup group) {
        boolean imageGroup = group == FileGroup.PROFILE_IMAGE || group == FileGroup.ROOM_IMAGE;
        String extension = extensionOf(file.getOriginalFilename());
        if (!(imageGroup ? PROFILE_EXTENSIONS : ATTACHMENT_EXTENSIONS).contains(extension)) {
            throw new BusinessException(FileErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        if (file.getSize() > (imageGroup ? IMAGE_LIMIT : ATTACHMENT_LIMIT)) {
            throw new BusinessException(FileErrorCode.FILE_SIZE_EXCEEDED);
        }
        if (storedFileRepository.sumSizeByRoomId(roomId) + file.getSize() > ROOM_STORAGE_LIMIT) {
            throw new BusinessException(FileErrorCode.FILE_STORAGE_LIMIT_EXCEEDED);
        }
        String directory = (imageGroup ? PUBLIC_DIR : PRIVATE_DIR).formatted(roomId, group.name().toLowerCase());
        String key = fileStoragePort.uploadFile(file, directory);
        return storedFileRepository.save(StoredFile.builder().ownerAccountId(accountId).roomId(roomId).fileGroup(group)
                .objectKey(key).originalName(file.getOriginalFilename()).mimeType(file.getContentType()).sizeBytes(file.getSize()).build());
    }

    private String extensionOf(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}
