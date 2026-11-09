package com.fiveoutoffive.moa_backend.ideacard.application.service;

import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import com.fiveoutoffive.moa_backend.dashboard.domain.repository.RoomWidgetRepository;
import com.fiveoutoffive.moa_backend.file.application.service.FileAttachSupport;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardSummaryResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.ReactionDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.ReactionSummaryResponse;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardFile;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardReaction;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardCommentRepository;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardFileRepository;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardReactionRepository;
import com.fiveoutoffive.moa_backend.mediation.domain.model.ConflictStatus;
import com.fiveoutoffive.moa_backend.mediation.domain.repository.CardConflictRepository;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 카드 목록·상세 응답 조립. 카드 수만큼 쿼리가 늘지 않도록 IN 으로 한 번에 읽는다. */
@Component
@RequiredArgsConstructor
public class IdeaCardViewAssembler {

    private final CardCommentRepository commentRepository;
    private final CardReactionRepository reactionRepository;
    private final CardFileRepository fileRepository;
    private final TaskRepository taskRepository;
    private final VoteRepository voteRepository;
    private final CardConflictRepository conflictRepository;
    private final RoomWidgetRepository widgetRepository;
    private final BoardSessionRepository sessionRepository;
    private final RoomAccessService access;
    private final FileAttachSupport fileAttachSupport;

    public List<IdeaCardSummaryResponse> summaries(Long roomId, Long viewerId, List<IdeaCard> cards) {
        if (cards.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = cards.stream().map(IdeaCard::getId).collect(Collectors.toSet());
        Map<Long, MemberRef> writers = access.memberRefs(roomId, cards.stream().map(IdeaCard::getWriterAccountId).collect(Collectors.toSet()));
        Map<Long, Long> comments = commentRepository.countByCardIds(ids).stream().collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        Map<Long, List<CardReaction>> reactions = reactionRepository.findByCardIdIn(ids).stream().collect(Collectors.groupingBy(CardReaction::getCardId));
        Map<Long, Long> files = fileRepository.findByCardIdIn(ids).stream().collect(Collectors.groupingBy(CardFile::getCardId, Collectors.counting()));
        Map<Long, Task> tasks = taskRepository.findBySourceCardIdInAndDeletedAtIsNull(ids).stream().collect(Collectors.toMap(Task::getSourceCardId, Function.identity()));
        Map<Long, Vote> votes = voteRepository.findBySourceCardIdInAndDeletedAtIsNull(ids).stream().collect(Collectors.toMap(Vote::getSourceCardId, Function.identity(), (a, b) -> a.getStatus() == VoteStatus.OPEN ? a : b));
        Map<Long, Long> conflicts = new HashMap<>();
        if (widgetRepository.existsByRoomIdAndWidgetType(roomId, WidgetType.MEDIATION)) {
            conflictRepository.findByRoomId(roomId).stream().filter(c -> c.getStatus() != ConflictStatus.DISMISSED).forEach(c -> {
                conflicts.merge(c.getCardAId(), 1L, Long::sum);
                conflicts.merge(c.getCardBId(), 1L, Long::sum);
            });
        }
        return cards.stream().map(card -> {
            Task task = tasks.get(card.getId());
            Vote vote = votes.get(card.getId());
            return new IdeaCardSummaryResponse(card.getId(), card.getCardType(), card.getTitle(), writers.get(card.getWriterAccountId()), card.getSortOrder(), card.getVersion(), card.getIsAiGenerated(),
                    comments.getOrDefault(card.getId(), 0L), files.getOrDefault(card.getId(), 0L), reactionSummary(reactions.getOrDefault(card.getId(), List.of()), viewerId),
                    task == null ? null : new IdeaCardSummaryResponse.ConvertedTask(task.getId(), task.getStatus()),
                    vote == null ? null : new IdeaCardSummaryResponse.LinkedVote(vote.getId(), vote.getStatus()),
                    conflicts.getOrDefault(card.getId(), 0L), card.getCreatedAt());
        }).toList();
    }

    public IdeaCardDetailResponse detail(Long roomId, Long viewerId, IdeaCard card) {
        boolean closed = sessionRepository.findById(card.getSessionId()).map(s -> s.getStatus() == SessionStatus.CLOSED).orElse(true);
        List<Long> fileIds = fileRepository.findByCardIdOrderBySortOrderAsc(card.getId()).stream().map(CardFile::getFileId).toList();
        List<IdeaCardDetailResponse.ConflictRef> conflicts = conflictRepository.findByRoomId(roomId).stream()
                .filter(c -> c.getCardAId().equals(card.getId()) || c.getCardBId().equals(card.getId()))
                .map(c -> new IdeaCardDetailResponse.ConflictRef(c.getId(), c.getCardAId().equals(card.getId()) ? c.getCardBId() : c.getCardAId(), c.getStatus())).toList();
        return new IdeaCardDetailResponse(summaries(roomId, viewerId, List.of(card)).getFirst(), card.getContent(), card.getSessionId(), closed, fileAttachSupport.responses(fileIds), reactionDetails(roomId, card.getId()), conflicts, card.getEditedAt());
    }

    public List<ReactionSummaryResponse> reactionSummary(List<CardReaction> reactions, Long viewerId) {
        return reactions.stream().collect(Collectors.groupingBy(CardReaction::getEmoji, LinkedHashMap::new, Collectors.toList())).entrySet().stream()
                .map(e -> new ReactionSummaryResponse(e.getKey(), e.getValue().size(), e.getValue().stream().anyMatch(r -> r.getAccountId().equals(viewerId)))).toList();
    }

    public List<ReactionDetailResponse> reactionDetails(Long roomId, Long cardId) {
        List<CardReaction> reactions = reactionRepository.findByCardId(cardId);
        Map<Long, MemberRef> refs = access.memberRefs(roomId, reactions.stream().map(CardReaction::getAccountId).collect(Collectors.toSet()));
        return reactions.stream().collect(Collectors.groupingBy(CardReaction::getEmoji, LinkedHashMap::new, Collectors.toList())).entrySet().stream()
                .map(e -> new ReactionDetailResponse(e.getKey(), e.getValue().size(), e.getValue().stream().map(r -> refs.get(r.getAccountId())).toList())).toList();
    }
}
