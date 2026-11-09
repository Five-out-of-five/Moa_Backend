package com.fiveoutoffive.moa_backend.vote.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.result.BriefingResponse;
import com.fiveoutoffive.moa_backend.briefing.application.service.MyTodoReader;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteBallot;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteCloseReason;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteBallotRepository;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class VoteScheduler implements MyTodoReader {

    private final VoteRepository voteRepository;
    private final VoteBallotRepository ballotRepository;
    private final VoteCloser closer;
    private final RoomAccessService access;
    private final NotificationSender notificationSender;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void closeExpired() {
        voteRepository.findByStatusAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus.OPEN, KstTime.nowUtc()).forEach(vote -> closer.close(vote, VoteCloseReason.DEADLINE, null));
    }

    /** 마감 1시간 전까지 참여하지 않은 멤버에게 투표 1건당 1회 알린다. */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void remindNonVoters() {
        voteRepository.findByStatusAndReminderSentAtIsNullAndDeadlineAtBeforeAndDeletedAtIsNull(VoteStatus.OPEN, KstTime.nowUtc().plusHours(1)).forEach(vote -> {
            voteRepository.save(vote.toBuilder().reminderSentAt(KstTime.nowUtc()).build());
            Set<Long> voted = ballotRepository.findByVoteId(vote.getId()).stream().map(VoteBallot::getAccountId).collect(Collectors.toSet());
            notificationSender.send(vote.getRoomId(), null, NotificationType.VOTE_REMINDER, access.activeMemberIds(vote.getRoomId()).stream().filter(id -> !voted.contains(id)).toList(), "투표 참여를 기다리고 있습니다", vote.getTitle(), "VOTE", vote.getId());
        });
    }

    @Override
    public Map<String, List<BriefingResponse.TodoItem>> read(Long roomId, Long accountId) {
        List<Vote> open = voteRepository.findByRoomIdAndStatusAndDeletedAtIsNull(roomId, VoteStatus.OPEN);
        Set<Long> voted = open.isEmpty() ? Set.of() : new HashSet<>(ballotRepository.findVotedIds(accountId, open.stream().map(Vote::getId).toList()));
        return Map.of("unvotedVotes", open.stream().filter(v -> !voted.contains(v.getId())).map(v -> new BriefingResponse.TodoItem("VOTE", v.getId(), v.getTitle())).toList());
    }
}
