package com.fiveoutoffive.moa_backend.room.application.service;

import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.global.port.out.FileStoragePort;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomJoinRequestRepository;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 참여 요청 7일 만료, 삭제된 방의 실제 삭제. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomScheduler {

    private final RoomJoinRequestRepository joinRequestRepository;
    private final RoomRepository roomRepository;
    private final StoredFileRepository storedFileRepository;
    private final FileStoragePort fileStoragePort;

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void expireJoinRequests() {
        joinRequestRepository.findByStatusAndExpiresAtBefore(JoinRequestStatus.PENDING, KstTime.nowUtc())
                .forEach(request -> joinRequestRepository.save(request.toBuilder().status(JoinRequestStatus.EXPIRED).build()));
    }

    /** 스토리지 객체를 먼저 지운 뒤 행을 지운다. 순서가 반대면 스토리지에 고아 파일이 남는다. */
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void purgeDeletedRooms() {
        roomRepository.findByDeletedAtBefore(KstTime.nowUtc()).forEach(room -> {
            storedFileRepository.findByRoomId(room.getId()).forEach(file -> fileStoragePort.deleteFile(file.getObjectKey()));
            roomRepository.deleteById(room.getId());
        });
    }
}
