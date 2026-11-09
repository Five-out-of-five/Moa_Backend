package com.fiveoutoffive.moa_backend.mood.application.service;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.mood.application.command.MoodUpsertCommand;
import com.fiveoutoffive.moa_backend.mood.application.result.MoodResponse;
import com.fiveoutoffive.moa_backend.mood.application.result.TeamMoodResponse;
import com.fiveoutoffive.moa_backend.mood.application.usecase.MoodUseCase;
import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;
import com.fiveoutoffive.moa_backend.mood.domain.model.MoodStatus;
import com.fiveoutoffive.moa_backend.mood.domain.repository.MoodStatusRepository;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MoodService implements MoodUseCase {

    private final MoodStatusRepository repository;
    private final RoomAccessService access;
    private final RealtimePublisher realtime;

    @Override
    public MoodResponse upsertMine(Long accountId, Long roomId, MoodUpsertCommand command) {
        access.requireMember(roomId, accountId);
        LocalDate today = KstTime.todayKst();
        MoodStatus saved = repository.save(repository.findByRoomIdAndAccountIdAndRecordedDate(roomId, accountId, today)
                .map(found -> found.toBuilder().mood(command.mood()).comment(command.comment()).build())
                .orElseGet(() -> MoodStatus.builder().roomId(roomId).accountId(accountId).mood(command.mood()).comment(command.comment()).recordedDate(today).build()));
        MoodResponse response = new MoodResponse(accountId, saved.getMood(), saved.getComment(), saved.getRecordedDate(), saved.getUpdatedAt());
        realtime.toRoom(roomId, "MOOD_UPDATED", response, accountId);
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public TeamMoodResponse getTeam(Long accountId, Long roomId, String period) {
        access.requireMember(roomId, accountId);
        LocalDate today = KstTime.todayKst();
        List<Long> memberIds = access.activeMemberIds(roomId);
        Map<Long, MemberRef> refs = access.memberRefs(roomId, memberIds);
        Map<Long, MoodStatus> todays = repository.findByRoomIdAndRecordedDate(roomId, today).stream().collect(Collectors.toMap(MoodStatus::getAccountId, Function.identity()));
        List<TeamMoodResponse.MemberMood> members = memberIds.stream().map(id -> {
            MoodStatus mood = todays.get(id);
            MemberRef ref = refs.get(id);
            return new TeamMoodResponse.MemberMood(id, ref.displayName(), ref.profileImageUrl(), mood == null ? null : mood.getMood(), mood == null ? null : mood.getComment(), mood == null ? null : mood.getRecordedDate());
        }).toList();
        int days = switch (period) {
            case "DAYS_7" -> 7;
            case "DAYS_30" -> 30;
            default -> 0;
        };
        List<TeamMoodResponse.TrendPoint> trend = List.of();
        if (days > 0) {
            Map<LocalDate, List<MoodStatus>> byDate = repository.findByRoomIdAndRecordedDateBetween(roomId, today.minusDays(days - 1L), today).stream().collect(Collectors.groupingBy(MoodStatus::getRecordedDate));
            trend = IntStream.range(0, days).mapToObj(i -> today.minusDays(days - 1L - i))
                    .map(date -> new TeamMoodResponse.TrendPoint(date, count(byDate.getOrDefault(date, List.of())))).toList();
        }
        return new TeamMoodResponse(count(List.copyOf(todays.values())), members, trend);
    }

    private Map<Mood, Long> count(List<MoodStatus> moods) {
        Map<Mood, Long> counts = new java.util.EnumMap<>(Mood.class);
        for (Mood mood : Mood.values()) {
            counts.put(mood, moods.stream().filter(m -> m.getMood() == mood).count());
        }
        return counts;
    }
}
