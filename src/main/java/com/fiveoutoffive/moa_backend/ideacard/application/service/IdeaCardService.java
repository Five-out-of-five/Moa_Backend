package com.fiveoutoffive.moa_backend.ideacard.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.service.BoardEventRecorder;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.file.application.service.FileAttachSupport;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.file.exception.FileErrorCode;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.exception.GlobalErrorCode;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.ideacard.application.command.AttachFileCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.CardMoveCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.CommentCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.CommentUpdateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardCreateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.command.IdeaCardUpdateCommand;
import com.fiveoutoffive.moa_backend.ideacard.application.result.CommentResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.EditLockResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.IdeaCardSummaryResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.ReactionDetailResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.result.ReactionSummaryResponse;
import com.fiveoutoffive.moa_backend.ideacard.application.usecase.IdeaCardUseCase;
import com.fiveoutoffive.moa_backend.ideacard.domain.event.CardChangedEvent;
import com.fiveoutoffive.moa_backend.ideacard.domain.event.CardDeletedEvent;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardComment;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardFile;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.CardReaction;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardCommentRepository;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardFileRepository;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.CardReactionRepository;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.ideacard.exception.IdeaCardErrorCode;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.session.domain.model.BoardSession;
import com.fiveoutoffive.moa_backend.session.domain.model.SessionStatus;
import com.fiveoutoffive.moa_backend.session.domain.repository.BoardSessionRepository;
import com.fiveoutoffive.moa_backend.session.exception.SessionErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 아이디어 카드·리액션·댓글·첨부. */
@Service
@Transactional
@RequiredArgsConstructor
public class IdeaCardService implements IdeaCardUseCase {

    private static final Set<String> EMOJIS = Set.of("THUMBS_UP", "CLAP", "FIRE", "THINKING", "SAD");
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);

    private final IdeaCardRepository cardRepository;
    private final CardCommentRepository commentRepository;
    private final CardReactionRepository reactionRepository;
    private final CardFileRepository fileRepository;
    private final BoardSessionRepository sessionRepository;
    private final IdeaCardViewAssembler assembler;
    private final RoomAccessService access;
    private final FileAttachSupport fileAttachSupport;
    private final BoardEventRecorder eventRecorder;
    private final NotificationSender notificationSender;
    private final RealtimePublisher realtime;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public List<IdeaCardSummaryResponse> getCards(Long accountId, Long roomId, Long sessionId) {
        access.requireMember(roomId, accountId);
        Long target = sessionId;
        if (target == null) {
            target = sessionRepository.findFirstByRoomIdAndStatus(roomId, SessionStatus.IN_PROGRESS).map(BoardSession::getId).orElse(null);
            if (target == null) {
                return List.of();
            }
        } else {
            requireSession(roomId, target);
        }
        return assembler.summaries(roomId, accountId, cardRepository.findBySessionIdAndDeletedAtIsNullOrderBySortOrderAsc(target));
    }

    @Override
    public IdeaCardDetailResponse create(Long accountId, Long roomId, IdeaCardCreateCommand command) {
        access.requireMember(roomId, accountId);
        BoardSession session = sessionRepository.findFirstByRoomIdAndStatus(roomId, SessionStatus.IN_PROGRESS)
                .orElseThrow(() -> new BusinessException(SessionErrorCode.SESSION_NONE_IN_PROGRESS));
        List<Long> fileIds = command.fileIds() == null ? List.of() : command.fileIds();
        // 같은 파일 ID 중복은 FileAttachSupport 가 400 으로 막는다.
        fileAttachSupport.requireAttachable(roomId, accountId, fileIds, FileGroup.CARD_ATTACHMENT, 0);
        IdeaCard card = cardRepository.save(IdeaCard.builder().roomId(roomId).sessionId(session.getId()).cardType(command.cardType()).title(command.title()).content(command.content())
                .writerAccountId(accountId).sortOrder(cardRepository.findMaxSortOrder(session.getId(), command.cardType()) + 1).isAiGenerated(false).build());
        for (int i = 0; i < fileIds.size(); i++) {
            fileRepository.save(CardFile.builder().cardId(card.getId()).fileId(fileIds.get(i)).sortOrder(i).build());
        }
        eventRecorder.record(roomId, BoardEventType.CARD_CREATED, accountId, null, BoardEventTargetType.CARD, card.getId(), card.getTitle());
        eventPublisher.publishEvent(new CardChangedEvent(roomId, card.getId(), true));
        return broadcastDetail(roomId, accountId, card, "CARD_CREATED");
    }

    @Transactional(readOnly = true)
    @Override
    public IdeaCardDetailResponse getDetail(Long accountId, Long roomId, Long cardId) {
        access.requireMember(roomId, accountId);
        return assembler.detail(roomId, accountId, find(roomId, cardId));
    }

    @Override
    public IdeaCardDetailResponse update(Long accountId, Long roomId, Long cardId, IdeaCardUpdateCommand command) {
        access.requireMember(roomId, accountId);
        IdeaCard card = writable(roomId, cardId, accountId);
        String holder = redisTemplate.opsForValue().get(RedisKeys.CARD_LOCK_PREFIX + cardId);
        if (holder != null && !holder.equals(String.valueOf(accountId))) {
            throw new BusinessException(IdeaCardErrorCode.CARD_LOCKED);
        }
        IdeaCard saved = cardRepository.save(card.toBuilder().title(command.title() != null ? command.title() : card.getTitle())
                .content(command.content() != null ? command.content() : card.getContent()).editedAt(KstTime.nowUtc()).build());
        eventPublisher.publishEvent(new CardChangedEvent(roomId, cardId, false));
        return broadcastDetail(roomId, accountId, saved, "CARD_UPDATED");
    }

    @Override
    public EditLockResponse lock(Long accountId, Long roomId, Long cardId) {
        access.requireMember(roomId, accountId);
        writable(roomId, cardId, accountId);
        String key = RedisKeys.CARD_LOCK_PREFIX + cardId;
        String me = String.valueOf(accountId);
        boolean mine = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, me, LOCK_TTL)) || me.equals(redisTemplate.opsForValue().get(key));
        if (!mine) {
            throw new BusinessException(IdeaCardErrorCode.CARD_LOCKED);
        }
        redisTemplate.expire(key, LOCK_TTL);
        MemberRef ref = access.memberRef(roomId, accountId);
        realtime.toRoom(roomId, "CARD_LOCKED", Map.of("cardId", cardId, "lockedBy", accountId), accountId);
        return new EditLockResponse(new EditLockResponse.LockedBy(accountId, ref.displayName()), KstTime.nowUtc().plus(LOCK_TTL));
    }

    @Override
    public void unlock(Long accountId, Long roomId, Long cardId) {
        access.requireMember(roomId, accountId);
        String key = RedisKeys.CARD_LOCK_PREFIX + cardId;
        if (String.valueOf(accountId).equals(redisTemplate.opsForValue().get(key))) {
            redisTemplate.delete(key);
            realtime.toRoom(roomId, "CARD_UNLOCKED", Map.of("cardId", cardId), accountId);
        }
    }

    @Override
    public void delete(Long accountId, Long roomId, Long cardId) {
        access.requireMember(roomId, accountId);
        IdeaCard card = writable(roomId, cardId, accountId);
        LocalDateTime now = KstTime.nowUtc();
        cardRepository.save(card.toBuilder().deletedAt(now).build());
        commentRepository.findByCardIdAndDeletedAtIsNull(cardId).forEach(comment -> commentRepository.save(comment.toBuilder().content("").deletedAt(now).build()));
        fileAttachSupport.softDelete(fileRepository.findByCardIdOrderBySortOrderAsc(cardId).stream().map(CardFile::getFileId).toList());
        eventPublisher.publishEvent(new CardDeletedEvent(roomId, cardId));
        realtime.toRoom(roomId, "CARD_DELETED", Map.of("cardId", cardId), accountId);
    }

    @Override
    public List<IdeaCardSummaryResponse> move(Long accountId, Long roomId, Long cardId, CardMoveCommand command) {
        access.requireMember(roomId, accountId);
        IdeaCard card = find(roomId, cardId);
        if (!card.getVersion().equals(command.version())) {
            throw new BusinessException(IdeaCardErrorCode.CARD_VERSION_CONFLICT, assembler.summaries(roomId, accountId, List.of(card)).getFirst());
        }
        List<IdeaCard> changed = new ArrayList<>();
        if (card.getCardType() != command.cardType()) {
            List<IdeaCard> source = new ArrayList<>(cardRepository.findBySessionIdAndCardTypeAndDeletedAtIsNullOrderBySortOrderAsc(card.getSessionId(), card.getCardType()));
            source.removeIf(c -> c.getId().equals(cardId));
            changed.addAll(reorder(source));
        }
        List<IdeaCard> target = new ArrayList<>(cardRepository.findBySessionIdAndCardTypeAndDeletedAtIsNullOrderBySortOrderAsc(card.getSessionId(), command.cardType()));
        target.removeIf(c -> c.getId().equals(cardId));
        target.add(Math.min(command.toIndex(), target.size()), card.toBuilder().cardType(command.cardType()).build());
        changed.addAll(reorder(target));
        List<IdeaCardSummaryResponse> response = assembler.summaries(roomId, accountId, changed);
        realtime.toRoom(roomId, "CARD_MOVED", response, accountId);
        return response;
    }

    @Override
    public List<ReactionSummaryResponse> addReaction(Long accountId, Long roomId, Long cardId, String emoji) {
        access.requireMember(roomId, accountId);
        IdeaCard card = open(roomId, cardId);
        requireEmoji(emoji);
        if (reactionRepository.findByCardIdAndAccountIdAndEmoji(cardId, accountId, emoji).isEmpty()) {
            reactionRepository.save(CardReaction.builder().cardId(cardId).accountId(accountId).emoji(emoji).build());
            notificationSender.send(roomId, accountId, NotificationType.CARD_REACTED, List.of(card.getWriterAccountId()), "내 카드에 반응이 달렸습니다", card.getTitle(), "CARD", cardId);
        }
        return reactionChanged(roomId, cardId, accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReactionDetailResponse> getReactions(Long accountId, Long roomId, Long cardId) {
        access.requireMember(roomId, accountId);
        find(roomId, cardId);
        return assembler.reactionDetails(roomId, cardId);
    }

    @Override
    public List<ReactionSummaryResponse> removeReaction(Long accountId, Long roomId, Long cardId, String emoji) {
        access.requireMember(roomId, accountId);
        open(roomId, cardId);
        reactionRepository.findByCardIdAndAccountIdAndEmoji(cardId, accountId, emoji).ifPresent(r -> reactionRepository.deleteById(r.getId()));
        return reactionChanged(roomId, cardId, accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentResponse> getComments(Long accountId, Long roomId, Long cardId) {
        access.requireMember(roomId, accountId);
        find(roomId, cardId);
        List<CardComment> comments = commentRepository.findByCardIdOrderByCreatedAtAsc(cardId);
        Map<Long, MemberRef> refs = access.memberRefs(roomId, comments.stream().map(CardComment::getWriterAccountId).collect(Collectors.toSet()));
        return comments.stream().map(c -> toResponse(c, refs.get(c.getWriterAccountId()))).toList();
    }

    @Override
    public CommentResponse addComment(Long accountId, Long roomId, Long cardId, CommentCreateCommand command) {
        access.requireMember(roomId, accountId);
        IdeaCard card = open(roomId, cardId);
        CardComment saved = commentRepository.save(CardComment.builder().cardId(cardId).writerAccountId(accountId).content(command.content()).build());
        notificationSender.send(roomId, accountId, NotificationType.CARD_COMMENTED, List.of(card.getWriterAccountId()), "내 카드에 의견이 달렸습니다", card.getTitle(), "CARD", cardId);
        eventRecorder.record(roomId, BoardEventType.CARD_COMMENTED, accountId, card.getWriterAccountId(), BoardEventTargetType.CARD, cardId, card.getTitle());
        CommentResponse response = toResponse(saved, access.memberRef(roomId, accountId));
        realtime.toRoom(roomId, "COMMENT_CHANGED", Map.of("cardId", cardId), accountId);
        return response;
    }

    @Override
    public CommentResponse updateComment(Long accountId, Long roomId, Long cardId, Long commentId, CommentUpdateCommand command) {
        access.requireMember(roomId, accountId);
        open(roomId, cardId);
        CardComment saved = commentRepository.save(ownComment(cardId, commentId, accountId).toBuilder().content(command.content()).editedAt(KstTime.nowUtc()).build());
        realtime.toRoom(roomId, "COMMENT_CHANGED", Map.of("cardId", cardId), accountId);
        return toResponse(saved, access.memberRef(roomId, accountId));
    }

    /** 자리('삭제된 댓글입니다')는 남기고 본문은 즉시 파기한다. 신고된 댓글의 원문은 신고 스냅샷에 남아 있다. */
    @Override
    public void deleteComment(Long accountId, Long roomId, Long cardId, Long commentId) {
        access.requireMember(roomId, accountId);
        open(roomId, cardId);
        commentRepository.save(ownComment(cardId, commentId, accountId).toBuilder().content("").deletedAt(KstTime.nowUtc()).build());
        realtime.toRoom(roomId, "COMMENT_CHANGED", Map.of("cardId", cardId), accountId);
    }

    @Override
    public List<FileResponse> attachFile(Long accountId, Long roomId, Long cardId, AttachFileCommand command) {
        access.requireMember(roomId, accountId);
        writable(roomId, cardId, accountId);
        List<CardFile> current = fileRepository.findByCardIdOrderBySortOrderAsc(cardId);
        fileAttachSupport.requireAttachable(roomId, accountId, List.of(command.fileId()), FileGroup.CARD_ATTACHMENT, current.size());
        fileRepository.save(CardFile.builder().cardId(cardId).fileId(command.fileId()).sortOrder(current.size()).build());
        realtime.toRoom(roomId, "CARD_UPDATED", Map.of("cardId", cardId), accountId);
        return fileAttachSupport.responses(fileRepository.findByCardIdOrderBySortOrderAsc(cardId).stream().map(CardFile::getFileId).toList());
    }

    @Override
    public void removeFile(Long accountId, Long roomId, Long cardId, Long fileId) {
        access.requireMember(roomId, accountId);
        // 요구사항 27: 마감된 회차의 카드는 수정·삭제 불가(읽기 전용). 첨부 추가와 같은 검사를 한다.
        writable(roomId, cardId, accountId);
        CardFile link = fileRepository.findByCardIdAndFileId(cardId, fileId).orElseThrow(() -> new BusinessException(FileErrorCode.FILE_NOT_FOUND));
        fileRepository.deleteById(link.getId());
        fileAttachSupport.softDelete(List.of(fileId));
        realtime.toRoom(roomId, "CARD_UPDATED", Map.of("cardId", cardId), accountId);
    }

    private List<ReactionSummaryResponse> reactionChanged(Long roomId, Long cardId, Long accountId) {
        realtime.toRoom(roomId, "REACTION_CHANGED", Map.of("cardId", cardId), accountId);
        return assembler.reactionSummary(reactionRepository.findByCardId(cardId), accountId);
    }

    private List<IdeaCard> reorder(List<IdeaCard> cards) {
        List<IdeaCard> saved = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            saved.add(cardRepository.save(cards.get(i).toBuilder().sortOrder(i).build()));
        }
        return saved;
    }

    private IdeaCardDetailResponse broadcastDetail(Long roomId, Long accountId, IdeaCard card, String type) {
        IdeaCardDetailResponse response = assembler.detail(roomId, accountId, card);
        realtime.toRoom(roomId, type, response.summary(), accountId);
        return response;
    }

    private void requireEmoji(String emoji) {
        if (!EMOJIS.contains(emoji)) {
            throw new BusinessException(GlobalErrorCode.INVALID_REQUEST);
        }
    }

    private void requireSession(Long roomId, Long sessionId) {
        sessionRepository.findById(sessionId).filter(s -> s.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(SessionErrorCode.SESSION_NOT_FOUND));
    }

    private IdeaCard find(Long roomId, Long cardId) {
        return cardRepository.findById(cardId).filter(c -> c.getRoomId().equals(roomId) && c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(IdeaCardErrorCode.CARD_NOT_FOUND));
    }

    /** 진행 중 회차의 카드만 통과시킨다. */
    private IdeaCard open(Long roomId, Long cardId) {
        IdeaCard card = find(roomId, cardId);
        if (sessionRepository.findById(card.getSessionId()).map(s -> s.getStatus() == SessionStatus.CLOSED).orElse(true)) {
            throw new BusinessException(IdeaCardErrorCode.CARD_SESSION_CLOSED);
        }
        return card;
    }

    private IdeaCard writable(Long roomId, Long cardId, Long accountId) {
        IdeaCard card = find(roomId, cardId);
        requireWriter(card, accountId);
        return open(roomId, cardId);
    }

    private void requireWriter(IdeaCard card, Long accountId) {
        if (!card.getWriterAccountId().equals(accountId)) {
            throw new BusinessException(IdeaCardErrorCode.CARD_NOT_WRITER);
        }
    }

    private CardComment ownComment(Long cardId, Long commentId, Long accountId) {
        CardComment comment = commentRepository.findById(commentId).filter(c -> c.getCardId().equals(cardId) && c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(IdeaCardErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getWriterAccountId().equals(accountId)) {
            throw new BusinessException(IdeaCardErrorCode.COMMENT_NOT_WRITER);
        }
        return comment;
    }

    private CommentResponse toResponse(CardComment c, MemberRef writer) {
        boolean deleted = c.getDeletedAt() != null;
        return new CommentResponse(c.getId(), writer, deleted ? null : c.getContent(), deleted, c.getEditedAt() != null, c.getCreatedAt());
    }
}
