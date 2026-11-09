package com.fiveoutoffive.moa_backend.task.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.result.BriefingResponse;
import com.fiveoutoffive.moa_backend.briefing.application.service.MyTodoReader;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.TaskOpenCountReader;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberLeftEvent;
import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistory;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistoryType;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskHistoryRepository;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 담당자가 방을 나가면 미담당으로 바꾸고 등록자에게 알린다. 다른 도메인에 읽기 값도 제공한다. */
@Component
@RequiredArgsConstructor
public class TaskMemberHandlers implements TaskOpenCountReader, MyTodoReader {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;
    private final NotificationSender notificationSender;

    @EventListener
    public void onMemberLeft(RoomMemberLeftEvent event) {
        taskRepository.findByRoomIdAndAssigneeAccountIdAndDeletedAtIsNull(event.roomId(), event.accountId()).stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .forEach(task -> {
                    taskRepository.save(task.toBuilder().assigneeAccountId(null).build());
                    historyRepository.save(TaskHistory.builder().taskId(task.getId()).historyType(TaskHistoryType.UNASSIGNED).fromAssigneeId(event.accountId()).actorAccountId(event.accountId()).build());
                    notificationSender.send(task.getRoomId(), event.accountId(), NotificationType.TASK_UNASSIGNED, List.of(task.getRegistrantAccountId()), "담당자가 없어진 업무가 있습니다", task.getTitle(), "TASK", task.getId());
                });
    }

    @Override
    public long countAssignedOpen(Long roomId, Long accountId) {
        return taskRepository.countByRoomIdAndAssigneeAccountIdAndStatusNotAndDeletedAtIsNull(roomId, accountId, TaskStatus.DONE);
    }

    @Override
    public Map<String, List<BriefingResponse.TodoItem>> read(Long roomId, Long accountId) {
        List<Task> assigned = taskRepository.findByRoomIdAndAssigneeAccountIdAndDeletedAtIsNull(roomId, accountId).stream().filter(t -> t.getStatus() != TaskStatus.DONE).toList();
        List<BriefingResponse.TodoItem> returned = new ArrayList<>();
        List<BriefingResponse.TodoItem> plain = new ArrayList<>();
        for (Task task : assigned) {
            boolean wasRejected = accountId.equals(task.getRegistrantAccountId())
                    && historyRepository.findFirstByTaskIdAndHistoryTypeOrderByCreatedAtDesc(task.getId(), TaskHistoryType.REJECTED).isPresent();
            (wasRejected ? returned : plain).add(new BriefingResponse.TodoItem("TASK", task.getId(), task.getTitle()));
        }
        List<BriefingResponse.TodoItem> unassigned = taskRepository.findByRoomIdAndRegistrantAccountIdAndAssigneeAccountIdIsNullAndDeletedAtIsNull(roomId, accountId).stream()
                .map(t -> new BriefingResponse.TodoItem("TASK", t.getId(), t.getTitle())).toList();
        return Map.of("assignedTasks", plain, "returnedTasks", returned, "unassignedTasks", unassigned);
    }
}
