package com.fiveoutoffive.moa_backend.vote.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.service.BoardEventRecorder;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.vote.domain.event.VoteLifecycleEvent;
import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteBallot;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteCloseReason;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteBallotRepository;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** 마감 시각 도래·전원 참여·수동 마감이 모두 같은 처리를 한다. */
@Component
@RequiredArgsConstructor
public class VoteCloser {

    private final VoteRepository voteRepository;
    private final VoteBallotRepository ballotRepository;
    private final RoomAccessService access;
    private final BoardEventRecorder eventRecorder;
    private final NotificationSender notificationSender;
    private final RealtimePublisher realtime;
    private final ApplicationEventPublisher eventPublisher;

    public Vote close(Vote vote, VoteCloseReason reason, Long closedBy) {
        Vote saved = voteRepository.save(vote.toBuilder().status(VoteStatus.CLOSED).closeReason(reason).closedAt(KstTime.nowUtc()).closedByAccountId(closedBy).build());
        Long roomId = saved.getRoomId();
        eventRecorder.record(roomId, BoardEventType.VOTE_CLOSED, closedBy, null, BoardEventTargetType.VOTE, saved.getId(), saved.getTitle());
        Set<Long> voted = ballotRepository.findByVoteId(saved.getId()).stream().map(VoteBallot::getAccountId).collect(Collectors.toSet());
        List<Long> members = access.activeMemberIds(roomId).stream().filter(id -> !id.equals(saved.getCreatorAccountId())).toList();
        notificationSender.send(roomId, closedBy, NotificationType.VOTE_CLOSED, members.stream().filter(voted::contains).toList(), "투표가 마감되었습니다", saved.getTitle(), "VOTE", saved.getId());
        notificationSender.send(roomId, closedBy, NotificationType.VOTE_CLOSED, members.stream().filter(id -> !voted.contains(id)).toList(), "참여하지 못한 투표가 결정되었습니다", saved.getTitle(), "VOTE", saved.getId());
        realtime.toRoom(roomId, "VOTE_CLOSED", Map.of("voteId", saved.getId()), closedBy);
        eventPublisher.publishEvent(new VoteLifecycleEvent(roomId, saved.getId(), saved.getTitle(), true));
        return saved;
    }
}
