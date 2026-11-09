package com.fiveoutoffive.moa_backend.vote.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.service.BoardEventRecorder;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardType;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.ideacard.exception.IdeaCardErrorCode;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.vote.application.command.BallotCommand;
import com.fiveoutoffive.moa_backend.vote.application.command.RevoteCommand;
import com.fiveoutoffive.moa_backend.vote.application.command.VoteCreateCommand;
import com.fiveoutoffive.moa_backend.vote.application.command.VoteUpdateCommand;
import com.fiveoutoffive.moa_backend.vote.application.result.VoteDetailResponse;
import com.fiveoutoffive.moa_backend.vote.application.result.VoteSummaryResponse;
import com.fiveoutoffive.moa_backend.vote.application.usecase.VoteUseCase;
import com.fiveoutoffive.moa_backend.vote.domain.event.VoteLifecycleEvent;
import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteBallot;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteCloseReason;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteOption;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteType;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteBallotRepository;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteOptionRepository;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import com.fiveoutoffive.moa_backend.vote.exception.VoteErrorCode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VoteService implements VoteUseCase {

    private static final int MAX_OPTIONS = 10;

    private final VoteRepository voteRepository;
    private final VoteOptionRepository optionRepository;
    private final VoteBallotRepository ballotRepository;
    private final IdeaCardRepository cardRepository;
    private final VoteCloser closer;
    private final RoomAccessService access;
    private final BoardEventRecorder eventRecorder;
    private final NotificationSender notificationSender;
    private final RealtimePublisher realtime;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public PageResponse<VoteSummaryResponse> getVotes(Long accountId, Long roomId, VoteStatus status, Pageable pageable) {
        access.requireMember(roomId, accountId);
        Page<Vote> page = status == null ? voteRepository.findByRoomIdAndDeletedAtIsNull(roomId, pageable) : voteRepository.findByRoomIdAndStatusAndDeletedAtIsNull(roomId, status, pageable);
        List<Long> ids = page.getContent().stream().map(Vote::getId).toList();
        Set<Long> votedByMe = ids.isEmpty() ? Set.of() : new HashSet<>(ballotRepository.findVotedIds(accountId, ids));
        Map<Long, Long> counts = ids.isEmpty() ? Map.of() : ballotRepository.countParticipants(ids).stream().collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        long memberCount = access.activeMemberIds(roomId).size();
        return Pages.response(page.map(v -> new VoteSummaryResponse(v.getId(), v.getTitle(), v.getVoteType(), v.getStatus(), v.getDeadlineAt(), counts.getOrDefault(v.getId(), 0L), memberCount, votedByMe.contains(v.getId()), v.getSourceCardId())));
    }

    @Override
    public VoteDetailResponse create(Long accountId, Long roomId, VoteCreateCommand command) {
        access.requireMember(roomId, accountId);
        requireDeadline(KstTime.toUtc(command.deadlineAt()));
        List<String> options = command.voteType() == VoteType.YES_NO ? List.of("찬성", "반대") : command.options();
        requireOptions(options);
        int maxSelect = command.voteType() == VoteType.MULTIPLE ? (command.maxSelectCount() == null ? options.size() : command.maxSelectCount()) : 1;
        if (maxSelect > options.size()) {
            throw new BusinessException(VoteErrorCode.VOTE_MAX_SELECT_INVALID);
        }
        if (command.sourceCardId() != null) {
            IdeaCard card = cardRepository.findById(command.sourceCardId()).filter(c -> c.getRoomId().equals(roomId) && c.getDeletedAt() == null).orElseThrow(() -> new BusinessException(IdeaCardErrorCode.CARD_NOT_FOUND));
            if (card.getCardType() != CardType.AGENDA) {
                throw new BusinessException(IdeaCardErrorCode.CARD_NOT_AGENDA);
            }
            if (voteRepository.findFirstBySourceCardIdAndStatusAndDeletedAtIsNull(card.getId(), VoteStatus.OPEN).isPresent()) {
                throw new BusinessException(IdeaCardErrorCode.CARD_VOTE_ALREADY_OPEN);
            }
        }
        Vote vote = voteRepository.save(Vote.builder().roomId(roomId).sourceCardId(command.sourceCardId()).title(command.title()).voteType(command.voteType()).maxSelectCount(maxSelect).isAnonymous(command.anonymous()).status(VoteStatus.OPEN).deadlineAt(KstTime.toUtc(command.deadlineAt())).creatorAccountId(accountId).build());
        saveOptions(vote.getId(), options);
        return afterCreate(accountId, roomId, vote);
    }

    @Transactional(readOnly = true)
    @Override
    public VoteDetailResponse getDetail(Long accountId, Long roomId, Long voteId) {
        access.requireMember(roomId, accountId);
        return detail(accountId, find(roomId, voteId));
    }

    @Override
    public VoteDetailResponse update(Long accountId, Long roomId, Long voteId, VoteUpdateCommand command) {
        access.requireMember(roomId, accountId);
        Vote vote = openVote(roomId, voteId);
        requireManager(roomId, vote, accountId);
        boolean changesContent = command.title() != null || command.options() != null || command.maxSelectCount() != null;
        if (!vote.getVersion().equals(command.version())) {
            throw new BusinessException(VoteErrorCode.VOTE_VERSION_CONFLICT, detail(accountId, vote));
        }
        if (changesContent && ballotRepository.existsByVoteId(voteId)) {
            throw new BusinessException(VoteErrorCode.VOTE_HAS_PARTICIPANTS);
        }
        LocalDateTime deadline = KstTime.toUtc(command.deadlineAt());
        if (deadline != null) {
            requireDeadline(deadline);
        }
        Vote.VoteBuilder builder = vote.toBuilder();
        if (command.title() != null) {
            builder.title(command.title());
        }
        if (command.options() != null && vote.getVoteType() != VoteType.YES_NO) {
            requireOptions(command.options());
            optionRepository.deleteByVoteId(voteId);
            optionRepository.flush();
            saveOptions(voteId, command.options());
        }
        if (command.maxSelectCount() != null) {
            if (command.maxSelectCount() > optionRepository.findByVoteIdOrderBySortOrderAsc(voteId).size()) {
                throw new BusinessException(VoteErrorCode.VOTE_MAX_SELECT_INVALID);
            }
            builder.maxSelectCount(command.maxSelectCount());
        }
        boolean deadlineChanged = deadline != null && !deadline.equals(vote.getDeadlineAt());
        if (deadlineChanged) {
            builder.deadlineAt(deadline).reminderSentAt(null);
        }
        Vote saved = voteRepository.save(builder.build());
        if (deadlineChanged) {
            notificationSender.send(roomId, accountId, NotificationType.VOTE_DEADLINE_CHANGED, access.activeMemberIds(roomId), "투표 마감 시각이 변경되었습니다", saved.getTitle(), "VOTE", voteId);
        }
        VoteDetailResponse response = detail(accountId, saved);
        realtime.toRoom(roomId, "VOTE_UPDATED", Map.of("voteId", voteId), accountId);
        return response;
    }

    @Override
    public void delete(Long accountId, Long roomId, Long voteId) {
        access.requireMember(roomId, accountId);
        Vote vote = openVote(roomId, voteId);
        requireManager(roomId, vote, accountId);
        List<Long> voters = ballotRepository.findByVoteId(voteId).stream().map(VoteBallot::getAccountId).distinct().toList();
        voteRepository.save(vote.toBuilder().deletedAt(KstTime.nowUtc()).build());
        ballotRepository.deleteByVoteId(voteId);
        notificationSender.send(roomId, accountId, NotificationType.VOTE_DELETED, voters, "참여한 투표가 삭제되었습니다", vote.getTitle(), "VOTE", voteId);
        realtime.toRoom(roomId, "VOTE_DELETED", Map.of("voteId", voteId), accountId);
    }

    @Override
    public VoteDetailResponse cast(Long accountId, Long roomId, Long voteId, BallotCommand command) {
        access.requireMember(roomId, accountId);
        Vote vote = openVote(roomId, voteId);
        if (vote.getDeadlineAt().isBefore(KstTime.nowUtc())) {
            throw new BusinessException(VoteErrorCode.VOTE_CLOSED);
        }
        Set<Long> valid = optionRepository.findByVoteIdOrderBySortOrderAsc(voteId).stream().map(VoteOption::getId).collect(Collectors.toSet());
        Set<Long> chosen = new HashSet<>(command.optionIds());
        if (chosen.size() > vote.getMaxSelectCount() || !valid.containsAll(chosen)) {
            throw new BusinessException(VoteErrorCode.VOTE_SELECTION_INVALID);
        }
        ballotRepository.deleteByVoteIdAndAccountId(voteId, accountId);
        ballotRepository.flush();
        chosen.forEach(optionId -> ballotRepository.save(VoteBallot.builder().voteId(voteId).optionId(optionId).accountId(accountId).votedAt(KstTime.nowUtc()).build()));
        Set<Long> voters = ballotRepository.findByVoteId(voteId).stream().map(VoteBallot::getAccountId).collect(Collectors.toSet());
        realtime.toRoom(roomId, "VOTE_PARTICIPATION_CHANGED", Map.of("voteId", voteId, "participantCount", voters.size()), accountId);
        Vote current = vote;
        if (voters.containsAll(access.activeMemberIds(roomId))) {
            current = closer.close(vote, VoteCloseReason.ALL_VOTED, null);
        }
        return detail(accountId, current);
    }

    @Override
    public VoteDetailResponse close(Long accountId, Long roomId, Long voteId) {
        access.requireMember(roomId, accountId);
        Vote vote = openVote(roomId, voteId);
        requireManager(roomId, vote, accountId);
        return detail(accountId, closer.close(vote, VoteCloseReason.MANUAL, accountId));
    }

    @Override
    public VoteDetailResponse revote(Long accountId, Long roomId, Long voteId, RevoteCommand command) {
        access.requireMember(roomId, accountId);
        Vote vote = find(roomId, voteId);
        if (!vote.getCreatorAccountId().equals(accountId)) {
            throw new BusinessException(VoteErrorCode.VOTE_NO_PERMISSION);
        }
        if (vote.getStatus() != VoteStatus.CLOSED) {
            throw new BusinessException(VoteErrorCode.VOTE_NOT_CLOSED);
        }
        requireDeadline(KstTime.toUtc(command.deadlineAt()));
        List<VoteDetailResponse.Option> options = options(vote);
        if (!isTied(options)) {
            throw new BusinessException(VoteErrorCode.VOTE_NOT_TIED);
        }
        Vote created = voteRepository.save(Vote.builder().roomId(roomId).sourceCardId(vote.getSourceCardId()).parentVoteId(voteId).title(vote.getTitle()).voteType(vote.getVoteType()).maxSelectCount(1).isAnonymous(vote.getIsAnonymous()).status(VoteStatus.OPEN).deadlineAt(KstTime.toUtc(command.deadlineAt())).creatorAccountId(accountId).build());
        saveOptions(created.getId(), options.stream().filter(VoteDetailResponse.Option::top).map(VoteDetailResponse.Option::content).toList());
        return afterCreate(accountId, roomId, created);
    }

    private VoteDetailResponse afterCreate(Long accountId, Long roomId, Vote vote) {
        eventRecorder.record(roomId, BoardEventType.VOTE_CREATED, accountId, null, BoardEventTargetType.VOTE, vote.getId(), vote.getTitle());
        notificationSender.send(roomId, accountId, NotificationType.VOTE_CREATED, access.activeMemberIds(roomId), "새 투표가 등록되었습니다", vote.getTitle(), "VOTE", vote.getId());
        realtime.toRoom(roomId, "VOTE_CREATED", Map.of("voteId", vote.getId()), accountId);
        eventPublisher.publishEvent(new VoteLifecycleEvent(roomId, vote.getId(), vote.getTitle(), false));
        return detail(accountId, vote);
    }

    private VoteDetailResponse detail(Long viewerId, Vote vote) {
        List<VoteBallot> ballots = ballotRepository.findByVoteId(vote.getId());
        Set<Long> voterIds = ballots.stream().map(VoteBallot::getAccountId).collect(Collectors.toSet());
        Long roomId = vote.getRoomId();
        List<VoteDetailResponse.Option> options = options(vote);
        VoteDetailResponse.SourceCard card = vote.getSourceCardId() == null ? null : cardRepository.findById(vote.getSourceCardId()).map(c -> new VoteDetailResponse.SourceCard(c.getId(), c.getTitle())).orElse(null);
        List<MemberRef> participants = Boolean.TRUE.equals(vote.getIsAnonymous()) ? List.of() : List.copyOf(access.memberRefs(roomId, voterIds).values());
        return new VoteDetailResponse(vote.getId(), vote.getTitle(), vote.getVoteType(), vote.getMaxSelectCount(), vote.getIsAnonymous(), vote.getStatus(), vote.getDeadlineAt(), vote.getCloseReason(), access.memberRef(roomId, vote.getCreatorAccountId()), card, vote.getParentVoteId(), vote.getVersion(),
                voterIds.size(), access.activeMemberIds(roomId).size(), participants, ballots.stream().filter(b -> b.getAccountId().equals(viewerId)).map(VoteBallot::getOptionId).toList(), options, vote.getStatus() == VoteStatus.CLOSED && isTied(options));
    }

    /** 마감 전에는 선택지별 득표 수를 내려주지 않는다. */
    private List<VoteDetailResponse.Option> options(Vote vote) {
        boolean closed = vote.getStatus() == VoteStatus.CLOSED;
        Map<Long, Long> counts = ballotRepository.findByVoteId(vote.getId()).stream().collect(Collectors.groupingBy(VoteBallot::getOptionId, Collectors.counting()));
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return optionRepository.findByVoteIdOrderBySortOrderAsc(vote.getId()).stream().map(o -> {
            long count = counts.getOrDefault(o.getId(), 0L);
            return new VoteDetailResponse.Option(o.getId(), o.getContent(), closed ? count : null, closed && max > 0 && count == max);
        }).toList();
    }

    private boolean isTied(List<VoteDetailResponse.Option> options) {
        return options.stream().filter(VoteDetailResponse.Option::top).count() > 1;
    }

    private void saveOptions(Long voteId, List<String> options) {
        for (int i = 0; i < options.size(); i++) {
            optionRepository.save(VoteOption.builder().voteId(voteId).content(options.get(i)).sortOrder(i).build());
        }
    }

    private void requireOptions(List<String> options) {
        if (options == null || options.size() < 2 || options.size() > MAX_OPTIONS) {
            throw new BusinessException(VoteErrorCode.VOTE_OPTION_COUNT_INVALID);
        }
    }

    private void requireDeadline(LocalDateTime deadline) {
        if (deadline.isBefore(KstTime.nowUtc().plusHours(1))) {
            throw new BusinessException(VoteErrorCode.VOTE_DEADLINE_TOO_SOON);
        }
    }

    private void requireManager(Long roomId, Vote vote, Long accountId) {
        if (!vote.getCreatorAccountId().equals(accountId) && !access.isOwner(roomId, accountId)) {
            throw new BusinessException(VoteErrorCode.VOTE_NO_PERMISSION);
        }
    }

    private Vote find(Long roomId, Long voteId) {
        return voteRepository.findById(voteId).filter(v -> v.getRoomId().equals(roomId) && v.getDeletedAt() == null).orElseThrow(() -> new BusinessException(VoteErrorCode.VOTE_NOT_FOUND));
    }

    private Vote openVote(Long roomId, Long voteId) {
        Vote vote = find(roomId, voteId);
        if (vote.getStatus() == VoteStatus.CLOSED) {
            throw new BusinessException(VoteErrorCode.VOTE_CLOSED);
        }
        return vote;
    }
}
