package com.fiveoutoffive.moa_backend.admin.application.service;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.repository.AccountRepository;
import com.fiveoutoffive.moa_backend.admin.application.command.RoomInactivateCommand;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminRoomDetailResponse;
import com.fiveoutoffive.moa_backend.admin.application.result.AdminRoomResponse;
import com.fiveoutoffive.moa_backend.admin.application.usecase.AdminRoomUseCase;
import com.fiveoutoffive.moa_backend.admin.exception.AdminErrorCode;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.report.domain.repository.ReportRepository;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.room.domain.model.Room;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomStatus;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import com.fiveoutoffive.moa_backend.room.exception.RoomErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminRoomService implements AdminRoomUseCase {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final ReportRepository reportRepository;
    private final AdminAuditRecorder audit;
    private final NotificationSender notificationSender;
    private final RealtimePublisher realtime;
    private final RoomAccessService access;

    @Transactional(readOnly = true)
    @Override
    public PageResponse<AdminRoomResponse> getRooms(String keyword, RoomStatus status, Integer inactiveDays, boolean orderByReportCount, Pageable pageable) {
        LocalDateTime before = inactiveDays == null ? null : KstTime.nowUtc().minusDays(inactiveDays);
        var rooms = orderByReportCount
                ? roomRepository.searchOrderByReportCount(keyword, status, before, pageable)
                : roomRepository.search(keyword, status, before, pageable);
        return Pages.response(rooms.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    @Override
    public AdminRoomDetailResponse getRoom(Long roomId) {
        return detail(require(roomId));
    }

    @Override
    public AdminRoomDetailResponse inactivate(Long adminId, Long roomId, RoomInactivateCommand command) {
        Room room = require(roomId);
        if (room.getStatus() == RoomStatus.INACTIVE) {
            throw new BusinessException(AdminErrorCode.ADMIN_ROOM_ALREADY_INACTIVE);
        }
        Room saved = roomRepository.save(room.toBuilder().status(RoomStatus.INACTIVE).inactiveReason(command.reason()).inactivatedAt(KstTime.nowUtc()).inactivatedByAccountId(adminId).build());
        access.ownerId(roomId).ifPresent(owner -> notificationSender.send(roomId, adminId, NotificationType.ROOM_INACTIVATED, List.of(owner), "방이 비활성 처리되었습니다", command.reason(), "ROOM", roomId));
        realtime.toRoom(roomId, "ROOM_INACTIVATED", Map.of("roomId", roomId), adminId);
        audit.record(adminId, "ROOM_INACTIVATE", "ROOM", roomId, Map.of("reason", command.reason()));
        return detail(saved);
    }

    @Override
    public AdminRoomDetailResponse reactivate(Long adminId, Long roomId) {
        Room room = require(roomId);
        if (room.getStatus() != RoomStatus.INACTIVE) {
            throw new BusinessException(AdminErrorCode.ADMIN_ROOM_NOT_INACTIVE);
        }
        Room saved = roomRepository.save(room.toBuilder().status(RoomStatus.ACTIVE).inactiveReason(null).inactivatedAt(null).inactivatedByAccountId(null).build());
        access.ownerId(roomId).ifPresent(owner -> notificationSender.send(roomId, adminId, NotificationType.ROOM_INACTIVATED, List.of(owner), "방이 다시 활성화되었습니다", null, "ROOM", roomId));
        audit.record(adminId, "ROOM_REACTIVATE", "ROOM", roomId, null);
        return detail(saved);
    }

    private Room require(Long roomId) {
        return roomRepository.findById(roomId).filter(r -> r.getDeletedAt() == null).orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND));
    }

    private AdminRoomResponse.Person person(Long accountId) {
        if (accountId == null) {
            return null;
        }
        return new AdminRoomResponse.Person(accountId, accountRepository.findById(accountId).map(Account::getName).orElse(null));
    }

    private AdminRoomResponse toResponse(Room r) {
        return new AdminRoomResponse(r.getId(), r.getName(), person(r.getCreatedByAccountId()), memberRepository.countByRoomIdAndStatus(r.getId(), RoomMemberStatus.ACTIVE), r.getStatus(), r.getLastActivityAt(), reportRepository.countByRoomId(r.getId()), r.getCreatedAt());
    }

    private AdminRoomDetailResponse detail(Room r) {
        return new AdminRoomDetailResponse(toResponse(r), r.getDescription(), r.getInactiveReason(), r.getInactivatedAt(), person(r.getInactivatedByAccountId()));
    }
}
