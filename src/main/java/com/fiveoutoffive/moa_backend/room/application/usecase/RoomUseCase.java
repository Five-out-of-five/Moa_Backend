package com.fiveoutoffive.moa_backend.room.application.usecase;

import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import java.time.LocalDateTime;
import com.fiveoutoffive.moa_backend.room.application.command.MemberProfileUpdateCommand;
import com.fiveoutoffive.moa_backend.room.application.command.OwnerDelegationCommand;
import com.fiveoutoffive.moa_backend.room.application.command.RoomCreateCommand;
import com.fiveoutoffive.moa_backend.room.application.command.RoomDeleteCommand;
import com.fiveoutoffive.moa_backend.room.application.command.RoomUpdateCommand;
import com.fiveoutoffive.moa_backend.room.application.result.InviteLinkResponse;
import com.fiveoutoffive.moa_backend.room.application.result.InvitePreviewResponse;
import com.fiveoutoffive.moa_backend.room.application.result.JoinRequestResponse;
import com.fiveoutoffive.moa_backend.room.application.result.LeaveCheckResponse;
import com.fiveoutoffive.moa_backend.room.application.result.MemberProfileResponse;
import com.fiveoutoffive.moa_backend.room.application.result.MemberResponse;
import com.fiveoutoffive.moa_backend.room.application.result.RoomDetailResponse;
import com.fiveoutoffive.moa_backend.room.application.result.RoomSummaryResponse;
import com.fiveoutoffive.moa_backend.room.domain.model.JoinRequestStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface RoomUseCase {

    RoomDetailResponse create(Long accountId, RoomCreateCommand command, MultipartFile image);

    List<RoomSummaryResponse> getMyRooms(Long accountId);

    RoomDetailResponse getRoom(Long accountId, Long roomId);

    RoomDetailResponse update(Long accountId, Long roomId, RoomUpdateCommand command);

    void delete(Long accountId, Long roomId, RoomDeleteCommand command);

    InviteLinkResponse getInviteLink(Long accountId, Long roomId);

    InvitePreviewResponse previewInvite(Long accountId, String inviteCode);

    JoinRequestResponse requestJoin(Long accountId, String inviteCode);

    PageResponse<JoinRequestResponse> getJoinRequests(Long accountId, Long roomId, JoinRequestStatus status, Pageable pageable);

    JoinRequestResponse approve(Long accountId, Long roomId, Long requestId);

    JoinRequestResponse reject(Long accountId, Long roomId, Long requestId);

    List<MemberResponse> getMembers(Long accountId, Long roomId);

    MemberProfileResponse getMemberProfile(Long accountId, Long roomId, Long targetId);

    MemberProfileResponse updateMyProfile(Long accountId, Long roomId, MemberProfileUpdateCommand command);

    LeaveCheckResponse checkLeave(Long accountId, Long roomId);

    void leave(Long accountId, Long roomId);

    void kick(Long accountId, Long roomId, Long targetId);

    List<MemberResponse> delegateOwner(Long accountId, Long roomId, OwnerDelegationCommand command);

    /**
     * 브리핑 '모두 확인': 활동 중인 멤버의 마지막 브리핑 확인 시각만 바꾼다(멤버 행 전체를 다시 저장하지 않음).
     * 그사이 나갔거나 강퇴됐으면 ROOM_NOT_MEMBER.
     */
    void markBriefingChecked(Long roomId, Long accountId, LocalDateTime checkedAt);
}
