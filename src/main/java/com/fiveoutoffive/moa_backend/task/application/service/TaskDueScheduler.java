package com.fiveoutoffive.moa_backend.task.application.service;

import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.task.domain.event.TaskDueSoonEvent;
import com.fiveoutoffive.moa_backend.task.domain.model.SprintStatus;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import com.fiveoutoffive.moa_backend.task.domain.repository.SprintRepository;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 스프린트 자동 마감, 마감 임박 알림. 날짜 판정은 한국 날짜 기준이다. */
@Component
@RequiredArgsConstructor
public class TaskDueScheduler {

    private final SprintRepository sprintRepository;
    private final SprintCloser sprintCloser;
    private final TaskRepository taskRepository;
    private final NotificationSender notificationSender;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void closeExpiredSprints() {
        sprintRepository.findByStatusAndEndDateBefore(SprintStatus.IN_PROGRESS, KstTime.todayKst()).forEach(sprint -> sprintCloser.close(sprint, null));
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void alertDueSoon() {
        taskRepository.findDueSoon(KstTime.todayKst().plusDays(1), TaskStatus.DONE).forEach(task -> {
            taskRepository.save(task.toBuilder().dueAlertSentAt(KstTime.nowUtc()).build());
            notificationSender.send(task.getRoomId(), null, NotificationType.TASK_DUE_SOON, List.of(task.getAssigneeAccountId()), "업무 마감이 임박했습니다", task.getTitle(), "TASK", task.getId());
            eventPublisher.publishEvent(new TaskDueSoonEvent(task.getRoomId(), task.getId(), task.getTitle()));
        });
    }
}
