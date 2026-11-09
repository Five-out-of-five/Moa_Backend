package com.fiveoutoffive.moa_backend.briefing.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.result.BriefingResponse;
import com.fiveoutoffive.moa_backend.briefing.application.usecase.BriefingUseCase;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEvent;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BriefingDismissal;
import com.fiveoutoffive.moa_backend.briefing.domain.repository.BoardEventRepository;
import com.fiveoutoffive.moa_backend.briefing.domain.repository.BriefingDismissalRepository;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMember;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomMemberRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BriefingService implements BriefingUseCase {

    private static final int MAX_ITEMS = 20;
    private static final int RETENTION_DAYS = 30;
    /** '팀 소식'에 건수로 보이는 이벤트 */
    private static final Set<BoardEventType> TEAM_NEWS = EnumSet.of(BoardEventType.CARD_CREATED, BoardEventType.TASK_CREATED, BoardEventType.TASK_COMPLETED,
            BoardEventType.VOTE_CREATED, BoardEventType.VOTE_CLOSED, BoardEventType.SESSION_CLOSED, BoardEventType.MEMBER_JOINED);

    private final BoardEventRepository eventRepository;
    private final BriefingDismissalRepository dismissalRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomAccessService access;
    private final BoardEventRecorder recorder;
    private final List<MyTodoReader> todoReaders;

    @Transactional(readOnly = true)
    @Override
    public BriefingResponse get(Long accountId, Long roomId) {
        return build(roomId, access.requireMember(roomId, accountId));
    }

    @Override
    public BriefingResponse checkAll(Long accountId, Long roomId) {
        RoomMember member = access.requireMember(roomId, accountId);
        return build(roomId, markChecked(member));
    }

    @Override
    public BriefingResponse dismiss(Long accountId, Long roomId, BoardEventType eventType) {
        RoomMember member = access.requireMember(roomId, accountId);
        LocalDateTime now = KstTime.nowUtc();
        dismissalRepository.save(dismissalRepository.findByRoomIdAndAccountIdAndEventType(roomId, accountId, eventType)
                .map(found -> found.toBuilder().dismissedAt(now).build())
                .orElseGet(() -> BriefingDismissal.builder().roomId(roomId).accountId(accountId).eventType(eventType).dismissedAt(now).build()));
        BriefingResponse response = build(roomId, member);
        // 모든 묶음을 지우면 '모두 확인'과 같이 처리한다.
        return response.teamNews().isEmpty() ? build(roomId, markChecked(member)) : response;
    }

    private RoomMember markChecked(RoomMember member) {
        dismissalRepository.deleteByRoomIdAndAccountId(member.getRoomId(), member.getAccountId());
        return roomMemberRepository.save(member.toBuilder().lastBriefingCheckedAt(KstTime.nowUtc()).build());
    }

    private BriefingResponse build(Long roomId, RoomMember member) {
        Long accountId = member.getAccountId();
        LocalDateTime checked = member.getLastBriefingCheckedAt();
        if (checked == null && member.getJoinedAt() != null && member.getJoinedAt().isAfter(KstTime.nowUtc().minusMinutes(1))) {
            return new BriefingResponse(false, null, new BriefingResponse.MyTodos(List.of(), List.of(), List.of(), List.of()), List.of(), new BriefingResponse.Highlights(List.of(), List.of()));
        }
        LocalDateTime limit = KstTime.nowUtc().minusDays(RETENTION_DAYS);
        LocalDateTime since = checked != null ? checked : limit;
        List<BoardEvent> events = eventRepository.findRecent(roomId, since, limit).stream().filter(e -> !accountId.equals(e.getActorAccountId())).toList();
        Map<BoardEventType, LocalDateTime> dismissed = dismissalRepository.findByRoomIdAndAccountId(roomId, accountId).stream()
                .collect(Collectors.toMap(BriefingDismissal::getEventType, BriefingDismissal::getDismissedAt));
        Map<Long, MemberRef> refs = access.memberRefs(roomId, events.stream().map(BoardEvent::getActorAccountId).filter(Objects::nonNull).collect(Collectors.toSet()));

        List<BriefingResponse.TeamNews> news = new ArrayList<>();
        for (BoardEventType type : BoardEventType.values()) {
            if (!TEAM_NEWS.contains(type)) {
                continue;
            }
            List<BoardEvent> ofType = events.stream().filter(e -> e.getEventType() == type)
                    .filter(e -> dismissed.get(type) == null || e.getCreatedAt().isAfter(dismissed.get(type))).toList();
            if (!ofType.isEmpty()) {
                news.add(new BriefingResponse.TeamNews(type, ofType.size(), ofType.stream().limit(MAX_ITEMS).map(e -> item(e, refs)).toList()));
            }
        }
        Map<String, List<BriefingResponse.TodoItem>> todos = new HashMap<>();
        todoReaders.forEach(reader -> todos.putAll(reader.read(roomId, accountId)));
        List<BriefingResponse.NewsItem> comments = events.stream().filter(e -> e.getEventType() == BoardEventType.CARD_COMMENTED && accountId.equals(e.getRecipientAccountId())).limit(MAX_ITEMS).map(e -> item(e, refs)).toList();
        List<BriefingResponse.NewsItem> converted = events.stream().filter(e -> e.getEventType() == BoardEventType.CARD_CONVERTED && accountId.equals(e.getRecipientAccountId())).limit(MAX_ITEMS).map(e -> item(e, refs)).toList();
        return new BriefingResponse(true, checked,
                new BriefingResponse.MyTodos(todos.getOrDefault("assignedTasks", List.of()), todos.getOrDefault("returnedTasks", List.of()), todos.getOrDefault("unassignedTasks", List.of()), todos.getOrDefault("unvotedVotes", List.of())),
                news, new BriefingResponse.Highlights(comments, converted));
    }

    private BriefingResponse.NewsItem item(BoardEvent e, Map<Long, MemberRef> refs) {
        MemberRef actor = refs.get(e.getActorAccountId());
        return new BriefingResponse.NewsItem(e.getId(), e.getTargetType(), e.getTargetId(), recorder.titleOf(e), actor == null ? null : actor.displayName(), e.getCreatedAt());
    }
}
