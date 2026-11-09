package com.fiveoutoffive.moa_backend.file.application.service;

import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.global.port.out.FileStoragePort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 어디에도 연결되지 않은 업로드를 하루 뒤 정리한다. */
@Component
@RequiredArgsConstructor
public class FileScheduler {

    private final StoredFileRepository storedFileRepository;
    private final FileStoragePort fileStoragePort;

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void cleanUnattached() {
        storedFileRepository.findUnattachedBefore(KstTime.nowUtc().minusDays(1)).forEach(file -> {
            fileStoragePort.deleteFile(file.getObjectKey());
            storedFileRepository.deleteById(file.getId());
        });
    }
}
