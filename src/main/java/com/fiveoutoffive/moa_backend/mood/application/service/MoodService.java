package com.fiveoutoffive.moa_backend.mood.application.service;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.mood.application.command.MoodUpsertCommand;
import com.fiveoutoffive.moa_backend.mood.application.result.MoodResponse;
import com.fiveoutoffive.moa_backend.mood.application.result.TeamMoodResponse;
import com.fiveoutoffive.moa_backend.mood.application.usecase.MoodUseCase;
import com.fiveoutoffive.moa_backend.mood.domain.model.Mood;
import com.fiveoutoffive.moa_backend.mood.domain.model.MoodPeriod;
import com.fiveoutoffive.moa_backend.mood.domain.model.MoodStatus;
import com.fiveoutoffive.moa_backend.mood.domain.repository.MoodStatusRepository;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        // 오늘 첫 기분을 빠르게 두 번 바꿔도(요청이 동시에 와도) 500 없이 한 건만 남도록 한 번에 넣거나 바꾼다.
        MoodStatus saved = repository.upsert(roomId, accountId, command.mood(), command.comment(), today, KstTime.nowUtc());
        MoodResponse response = new MoodResponse(accountId, saved.getMood(), saved.getComment(), saved.getRecordedDate(), saved.getUpdatedAt());
        realtime.toRoom(roomId, "MOOD_UPDATED", response, accountId);
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public TeamMoodResponse getTeam(Long accountId, Long roomId, MoodPeriod period) {
        access.requireMember(roomId, accountId);
        LocalDate today = KstTime.todayKst();
        List<Long> memberIds = access.activeMemberIds(roomId);
        Map<Long, MemberRef> refs = access.memberRefs(roomId, memberIds);
        // 오늘 기분을 남기고 나간 멤버는 목록(members)에 없으므로 분포에서도 뺀다. 그래야 "N명 참여"와 목록이 맞는다.
        Set<Long> active = Set.copyOf(memberIds);
        Map<Long, MoodStatus> todays = repository.findByRoomIdAndRecordedDate(roomId, today).stream()
                .filter(mood -> active.contains(mood.getAccountId()))
                .collect(Collectors.toMap(MoodStatus::getAccountId, Function.identity()));
        List<TeamMoodResponse.MemberMood> members = memberIds.stream().map(id -> {
            MoodStatus mood = todays.get(id);
            MemberRef ref = refs.get(id);
            return new TeamMoodResponse.MemberMood(id, ref.displayName(), ref.profileImageUrl(), mood == null ? null : mood.getMood(), mood == null ? null : mood.getComment(), mood == null ? null : mood.getRecordedDate());
        }).toList();
        int days = period.days();
        List<TeamMoodResponse.TrendPoint> trend = List.of();
        if (days > 0) {
            Map<LocalDate, List<MoodStatus>> byDate = repository.findByRoomIdAndRecordedDateBetween(roomId, today.minusDays(days - 1L), today).stream().collect(Collectors.groupingBy(MoodStatus::getRecordedDate));
            // 지난 날은 그날 기록 그대로 두고, 오늘은 위 분포와 같은 기준(현재 멤버)으로 센다.
            byDate.put(today, List.copyOf(todays.values()));
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
