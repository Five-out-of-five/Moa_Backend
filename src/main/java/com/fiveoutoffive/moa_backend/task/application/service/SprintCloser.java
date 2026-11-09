package com.fiveoutoffive.moa_backend.task.application.service;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.task.domain.model.Sprint;
import com.fiveoutoffive.moa_backend.task.domain.model.SprintStatus;
import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import com.fiveoutoffive.moa_backend.task.domain.repository.SprintRepository;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 마감 시점 업무 수를 기록하고 미완료 업무를 백로그로 옮긴다. */
@Component
@RequiredArgsConstructor
public class SprintCloser {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final RealtimePublisher realtime;

    public Sprint close(Sprint sprint, Long closedBy) {
        List<Task> tasks = taskRepository.findBySprintIdAndDeletedAtIsNull(sprint.getId());
        int done = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        tasks.stream().filter(t -> t.getStatus() != TaskStatus.DONE).forEach(t -> taskRepository.save(t.toBuilder().sprintId(null).build()));
        Sprint saved = sprintRepository.save(sprint.toBuilder().status(SprintStatus.CLOSED).closedAt(KstTime.nowUtc()).closedByAccountId(closedBy).totalTaskCount(tasks.size()).doneTaskCount(done).build());
        realtime.toRoom(saved.getRoomId(), "SPRINT_CLOSED", Map.of("sprintId", saved.getId()), closedBy);
        return saved;
    }
}
