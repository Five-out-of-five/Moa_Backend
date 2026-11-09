package com.fiveoutoffive.moa_backend.task.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.service.BoardEventRecorder;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import com.fiveoutoffive.moa_backend.file.application.result.FileResponse;
import com.fiveoutoffive.moa_backend.file.application.service.FileAttachSupport;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.exception.FileErrorCode;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.common.api.response.PageResponse;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.Pages;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.ideacard.domain.model.IdeaCard;
import com.fiveoutoffive.moa_backend.ideacard.domain.repository.IdeaCardRepository;
import com.fiveoutoffive.moa_backend.ideacard.exception.IdeaCardErrorCode;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.task.application.command.AssigneeChangeCommand;
import com.fiveoutoffive.moa_backend.task.application.command.SprintStartCommand;
import com.fiveoutoffive.moa_backend.task.application.command.SprintUpdateCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskAttachFileCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskCreateCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskMoveCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskSprintChangeCommand;
import com.fiveoutoffive.moa_backend.task.application.command.TaskUpdateCommand;
import com.fiveoutoffive.moa_backend.task.application.result.ProgressResponse;
import com.fiveoutoffive.moa_backend.task.application.result.SprintResponse;
import com.fiveoutoffive.moa_backend.task.application.result.TaskDetailResponse;
import com.fiveoutoffive.moa_backend.task.application.result.TaskSummaryResponse;
import com.fiveoutoffive.moa_backend.task.application.result.WorkloadResponse;
import com.fiveoutoffive.moa_backend.task.application.usecase.TaskUseCase;
import com.fiveoutoffive.moa_backend.task.domain.model.Sprint;
import com.fiveoutoffive.moa_backend.task.domain.model.SprintStatus;
import com.fiveoutoffive.moa_backend.task.domain.model.Task;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskFile;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistory;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistoryType;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskPriority;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskSourceType;
import com.fiveoutoffive.moa_backend.task.domain.model.TaskStatus;
import com.fiveoutoffive.moa_backend.task.domain.repository.SprintRepository;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskFileRepository;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskHistoryRepository;
import com.fiveoutoffive.moa_backend.task.domain.repository.TaskRepository;
import com.fiveoutoffive.moa_backend.task.exception.TaskErrorCode;
import com.fiveoutoffive.moa_backend.vote.domain.model.Vote;
import com.fiveoutoffive.moa_backend.vote.domain.model.VoteStatus;
import com.fiveoutoffive.moa_backend.vote.domain.repository.VoteRepository;
import com.fiveoutoffive.moa_backend.vote.exception.VoteErrorCode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 업무·스프린트·진척도·밸런서. */
@Service
@Transactional
@RequiredArgsConstructor
public class TaskService implements TaskUseCase {

    private static final int DUE_SOON_DAYS = 3;

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;
    private final TaskFileRepository fileRepository;
    private final SprintRepository sprintRepository;
    private final SprintCloser sprintCloser;
    private final IdeaCardRepository cardRepository;
    private final VoteRepository voteRepository;
    private final RoomAccessService access;
    private final FileAttachSupport fileAttachSupport;
    private final BoardEventRecorder eventRecorder;
    private final NotificationSender notificationSender;
    private final RealtimePublisher realtime;

    @Transactional(readOnly = true)
    @Override
    public List<TaskSummaryResponse> getTasks(Long accountId, Long roomId, TaskStatus status, Long assigneeId, TaskPriority priority, Long sprintId, boolean backlog) {
        access.requireMember(roomId, accountId);
        List<Task> tasks = taskRepository.findByRoomIdAndDeletedAtIsNull(roomId).stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> assigneeId == null || assigneeId.equals(t.getAssigneeAccountId()))
                .filter(t -> priority == null || t.getPriority() == priority)
                .filter(t -> sprintId == null || sprintId.equals(t.getSprintId()))
                .filter(t -> !backlog || t.getSprintId() == null)
                .sorted(Comparator.comparing(Task::getSortOrder))
                .toList();
        return summaries(roomId, tasks);
    }

    @Override
    public TaskDetailResponse create(Long accountId, Long roomId, TaskCreateCommand command) {
        access.requireMember(roomId, accountId);
        if (!access.isActiveMember(roomId, command.assigneeAccountId())) {
            throw new BusinessException(TaskErrorCode.TASK_ASSIGNEE_NOT_MEMBER);
        }
        requireFutureDue(command.dueDate());
        if (command.sourceCardId() != null && command.sourceVoteId() != null) {
            throw new BusinessException(TaskErrorCode.TASK_SOURCE_INVALID);
        }
        IdeaCard sourceCard = command.sourceCardId() == null ? null : requireConvertibleCard(roomId, command.sourceCardId());
        if (command.sourceVoteId() != null) {
            Vote vote = voteRepository.findById(command.sourceVoteId()).filter(v -> v.getRoomId().equals(roomId) && v.getDeletedAt() == null)
                    .orElseThrow(() -> new BusinessException(VoteErrorCode.VOTE_NOT_FOUND));
            if (vote.getStatus() != VoteStatus.CLOSED) {
                throw new BusinessException(VoteErrorCode.VOTE_NOT_CLOSED);
            }
        }
        List<Long> fileIds = command.fileIds() == null ? List.of() : command.fileIds();
        fileAttachSupport.requireAttachable(roomId, accountId, fileIds, FileGroup.TASK_ATTACHMENT, 0);
        Long sprintId = sprintRepository.findFirstByRoomIdAndStatus(roomId, SprintStatus.IN_PROGRESS).map(Sprint::getId).orElse(null);
        TaskSourceType sourceType = sourceCard != null ? TaskSourceType.CARD : command.sourceVoteId() != null ? TaskSourceType.VOTE : TaskSourceType.NONE;
        Task task = taskRepository.save(Task.builder().roomId(roomId).sprintId(sprintId).title(command.title()).description(command.description()).status(TaskStatus.TODO)
                .priority(command.priority() != null ? command.priority() : TaskPriority.NORMAL).registrantAccountId(accountId).assigneeAccountId(command.assigneeAccountId()).dueDate(command.dueDate())
                .sortOrder(taskRepository.findMaxSortOrder(roomId, TaskStatus.TODO) + 1).sourceType(sourceType).sourceCardId(command.sourceCardId()).sourceVoteId(command.sourceVoteId()).build());
        for (int i = 0; i < fileIds.size(); i++) {
            fileRepository.save(TaskFile.builder().taskId(task.getId()).fileId(fileIds.get(i)).sortOrder(i).build());
        }
        history(task.getId(), TaskHistoryType.CREATED, null, TaskStatus.TODO, null, command.assigneeAccountId(), accountId);
        eventRecorder.record(roomId, BoardEventType.TASK_CREATED, accountId, null, BoardEventTargetType.TASK, task.getId(), task.getTitle());
        eventRecorder.record(roomId, BoardEventType.TASK_ASSIGNED, accountId, command.assigneeAccountId(), BoardEventTargetType.TASK, task.getId(), task.getTitle());
        notificationSender.send(roomId, accountId, NotificationType.TASK_ASSIGNED, List.of(command.assigneeAccountId()), "업무가 배정되었습니다", task.getTitle(), "TASK", task.getId());
        if (sourceCard != null) {
            eventRecorder.record(roomId, BoardEventType.CARD_CONVERTED, accountId, sourceCard.getWriterAccountId(), BoardEventTargetType.CARD, sourceCard.getId(), sourceCard.getTitle());
            notificationSender.send(roomId, accountId, NotificationType.CARD_CONVERTED, List.of(sourceCard.getWriterAccountId()), "내 카드가 업무로 전환되었습니다", sourceCard.getTitle(), "TASK", task.getId());
        }
        return broadcast(roomId, accountId, task, "TASK_CREATED");
    }

    @Transactional(readOnly = true)
    @Override
    public TaskDetailResponse getDetail(Long accountId, Long roomId, Long taskId) {
        access.requireMember(roomId, accountId);
        return detail(roomId, find(roomId, taskId));
    }

    @Override
    public TaskDetailResponse update(Long accountId, Long roomId, Long taskId, TaskUpdateCommand command) {
        access.requireMember(roomId, accountId);
        Task task = find(roomId, taskId);
        requireEditor(roomId, task, accountId);
        requireFutureDue(command.dueDate());
        boolean dueChanged = command.dueDate() != null && !command.dueDate().equals(task.getDueDate());
        Task saved = taskRepository.save(task.toBuilder()
                .title(command.title() != null ? command.title() : task.getTitle())
                .description(command.description() != null ? command.description() : task.getDescription())
                .dueDate(command.dueDate() != null ? command.dueDate() : task.getDueDate())
                .priority(command.priority() != null ? command.priority() : task.getPriority())
                .dueAlertSentAt(dueChanged ? null : task.getDueAlertSentAt())
                .build());
        return broadcast(roomId, accountId, saved, "TASK_UPDATED");
    }

    @Override
    public List<TaskSummaryResponse> move(Long accountId, Long roomId, Long taskId, TaskMoveCommand command) {
        access.requireMember(roomId, accountId);
        Task task = find(roomId, taskId);
        requireEditor(roomId, task, accountId);
        if (!task.getVersion().equals(command.version())) {
            throw new BusinessException(TaskErrorCode.TASK_VERSION_CONFLICT, summaries(roomId, List.of(task)).getFirst());
        }
        TaskStatus from = task.getStatus();
        TaskStatus to = command.status();
        Task updated = task.toBuilder().status(to).completedAt(to == TaskStatus.DONE ? (from == TaskStatus.DONE ? task.getCompletedAt() : KstTime.nowUtc()) : null).build();
        List<Task> changed = new ArrayList<>();
        if (from != to) {
            List<Task> source = new ArrayList<>(taskRepository.findByRoomIdAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(roomId, from));
            source.removeIf(t -> t.getId().equals(taskId));
            changed.addAll(reorder(source));
        }
        List<Task> target = new ArrayList<>(taskRepository.findByRoomIdAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(roomId, to));
        target.removeIf(t -> t.getId().equals(taskId));
        target.add(Math.min(command.toIndex(), target.size()), updated);
        changed.addAll(reorder(target));
        if (from != to) {
            history(taskId, TaskHistoryType.STATUS_CHANGED, from, to, null, null, accountId);
            notificationSender.send(roomId, accountId, NotificationType.TASK_STATUS_CHANGED, List.of(task.getRegistrantAccountId()), "등록한 업무의 상태가 변경되었습니다", task.getTitle(), "TASK", taskId);
            if (to == TaskStatus.DONE) {
                eventRecorder.record(roomId, BoardEventType.TASK_COMPLETED, accountId, null, BoardEventTargetType.TASK, taskId, task.getTitle());
            }
        }
        List<TaskSummaryResponse> response = summaries(roomId, changed);
        realtime.toRoom(roomId, "TASK_MOVED", response, accountId);
        return response;
    }

    @Override
    public TaskDetailResponse changeSprint(Long accountId, Long roomId, Long taskId, TaskSprintChangeCommand command) {
        access.requireMember(roomId, accountId);
        Task task = find(roomId, taskId);
        requireEditor(roomId, task, accountId);
        if (command.sprintId() != null) {
            Sprint sprint = sprintRepository.findById(command.sprintId()).filter(s -> s.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(TaskErrorCode.SPRINT_NOT_FOUND));
            if (sprint.getStatus() != SprintStatus.IN_PROGRESS) {
                throw new BusinessException(TaskErrorCode.SPRINT_NOT_IN_PROGRESS);
            }
        }
        return broadcast(roomId, accountId, taskRepository.save(task.toBuilder().sprintId(command.sprintId()).build()), "TASK_UPDATED");
    }

    @Override
    public TaskDetailResponse changeAssignee(Long accountId, Long roomId, Long taskId, AssigneeChangeCommand command) {
        access.requireMember(roomId, accountId);
        Task task = find(roomId, taskId);
        if (!accountId.equals(task.getRegistrantAccountId()) && !access.isOwner(roomId, accountId)) {
            throw new BusinessException(TaskErrorCode.TASK_NO_PERMISSION);
        }
        if (!access.isActiveMember(roomId, command.assigneeAccountId())) {
            throw new BusinessException(TaskErrorCode.TASK_ASSIGNEE_NOT_MEMBER);
        }
        Task saved = taskRepository.save(task.toBuilder().assigneeAccountId(command.assigneeAccountId()).build());
        history(taskId, TaskHistoryType.ASSIGNED, null, null, task.getAssigneeAccountId(), command.assigneeAccountId(), accountId);
        eventRecorder.record(roomId, BoardEventType.TASK_ASSIGNED, accountId, command.assigneeAccountId(), BoardEventTargetType.TASK, taskId, task.getTitle());
        notificationSender.send(roomId, accountId, NotificationType.TASK_ASSIGNED, List.of(command.assigneeAccountId()), "업무가 배정되었습니다", task.getTitle(), "TASK", taskId);
        return broadcast(roomId, accountId, saved, "TASK_UPDATED");
    }

    @Override
    public TaskDetailResponse reject(Long accountId, Long roomId, Long taskId) {
        access.requireMember(roomId, accountId);
        Task task = find(roomId, taskId);
        if (!accountId.equals(task.getAssigneeAccountId())) {
            throw new BusinessException(TaskErrorCode.TASK_NOT_ASSIGNEE);
        }
        if (accountId.equals(task.getRegistrantAccountId())) {
            throw new BusinessException(TaskErrorCode.TASK_CANNOT_REJECT_OWN);
        }
        Task saved = taskRepository.save(task.toBuilder().assigneeAccountId(task.getRegistrantAccountId()).build());
        history(taskId, TaskHistoryType.REJECTED, null, null, accountId, task.getRegistrantAccountId(), accountId);
        eventRecorder.record(roomId, BoardEventType.TASK_REJECTED, accountId, task.getRegistrantAccountId(), BoardEventTargetType.TASK, taskId, task.getTitle());
        notificationSender.send(roomId, accountId, NotificationType.TASK_REJECTED, List.of(task.getRegistrantAccountId()), "담당 업무가 반려되었습니다", task.getTitle(), "TASK", taskId);
        return broadcast(roomId, accountId, saved, "TASK_UPDATED");
    }

    @Override
    public void delete(Long accountId, Long roomId, Long taskId) {
        access.requireMember(roomId, accountId);
        Task task = find(roomId, taskId);
        if (!accountId.equals(task.getRegistrantAccountId()) && !access.isOwner(roomId, accountId)) {
            throw new BusinessException(TaskErrorCode.TASK_NO_PERMISSION);
        }
        taskRepository.save(task.toBuilder().deletedAt(KstTime.nowUtc()).build());
        if (task.getAssigneeAccountId() != null) {
            notificationSender.send(roomId, accountId, NotificationType.TASK_DELETED, List.of(task.getAssigneeAccountId()), "담당 업무가 삭제되었습니다", task.getTitle(), "TASK", taskId);
        }
        realtime.toRoom(roomId, "TASK_DELETED", Map.of("taskId", taskId), accountId);
    }

    @Override
    public List<FileResponse> attachFile(Long accountId, Long roomId, Long taskId, TaskAttachFileCommand command) {
        access.requireMember(roomId, accountId);
        Task task = find(roomId, taskId);
        if (!accountId.equals(task.getRegistrantAccountId()) && !accountId.equals(task.getAssigneeAccountId())) {
            throw new BusinessException(TaskErrorCode.TASK_NO_PERMISSION);
        }
        List<TaskFile> current = fileRepository.findByTaskIdOrderBySortOrderAsc(taskId);
        fileAttachSupport.requireAttachable(roomId, accountId, List.of(command.fileId()), FileGroup.TASK_ATTACHMENT, current.size());
        fileRepository.save(TaskFile.builder().taskId(taskId).fileId(command.fileId()).sortOrder(current.size()).build());
        realtime.toRoom(roomId, "TASK_UPDATED", Map.of("taskId", taskId), accountId);
        return fileAttachSupport.responses(fileRepository.findByTaskIdOrderBySortOrderAsc(taskId).stream().map(TaskFile::getFileId).toList());
    }

    @Override
    public void removeFile(Long accountId, Long roomId, Long taskId, Long fileId) {
        access.requireMember(roomId, accountId);
        Task task = find(roomId, taskId);
        StoredFile file = fileAttachSupport.find(roomId, fileId);
        if (!accountId.equals(task.getRegistrantAccountId()) && !accountId.equals(file.getOwnerAccountId())) {
            throw new BusinessException(TaskErrorCode.TASK_NO_PERMISSION);
        }
        TaskFile link = fileRepository.findByTaskIdAndFileId(taskId, fileId).orElseThrow(() -> new BusinessException(FileErrorCode.FILE_NOT_FOUND));
        fileRepository.deleteById(link.getId());
        fileAttachSupport.softDelete(List.of(fileId));
        realtime.toRoom(roomId, "TASK_UPDATED", Map.of("taskId", taskId), accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public ProgressResponse getProgress(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        Optional<Sprint> sprint = sprintRepository.findFirstByRoomIdAndStatus(roomId, SprintStatus.IN_PROGRESS);
        List<Task> tasks = sprint.map(s -> taskRepository.findBySprintIdAndDeletedAtIsNull(s.getId())).orElse(List.of());
        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status, tasks.stream().filter(t -> t.getStatus() == status).count());
        }
        long done = counts.get(TaskStatus.DONE);
        LocalDate limit = KstTime.todayKst().plusDays(DUE_SOON_DAYS);
        long dueSoon = tasks.stream().filter(t -> t.getStatus() != TaskStatus.DONE && t.getDueDate() != null && !t.getDueDate().isAfter(limit)).count();
        return new ProgressResponse(sprint.map(s -> new ProgressResponse.SprintInfo(s.getId(), s.getSprintNo(), s.getName(), s.getStartDate(), s.getEndDate())).orElse(null),
                tasks.size(), done, tasks.isEmpty() ? 0 : (int) (done * 100 / tasks.size()), counts, dueSoon);
    }

    @Transactional(readOnly = true)
    @Override
    public WorkloadResponse getWorkload(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        List<Long> memberIds = access.activeMemberIds(roomId);
        Map<Long, MemberRef> refs = access.memberRefs(roomId, memberIds);
        LocalDate limit = KstTime.todayKst().plusDays(DUE_SOON_DAYS);
        Map<Long, List<Task>> open = taskRepository.findByRoomIdAndDeletedAtIsNull(roomId).stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE && t.getAssigneeAccountId() != null).collect(Collectors.groupingBy(Task::getAssigneeAccountId));
        List<WorkloadResponse.MemberLoad> loads = memberIds.stream().map(id -> {
            List<Task> mine = open.getOrDefault(id, List.of());
            double score = mine.stream().mapToDouble(t -> weight(t.getPriority()) + (t.getDueDate() != null && !t.getDueDate().isAfter(limit) ? 1 : 0)).sum();
            return new WorkloadResponse.MemberLoad(id, refs.get(id).displayName(), score, score < 3 ? "RELAXED" : score < 6 ? "NORMAL" : "OVERLOADED", mine.size());
        }).sorted(Comparator.comparingDouble(WorkloadResponse.MemberLoad::score)).toList();
        double average = loads.stream().mapToDouble(WorkloadResponse.MemberLoad::score).average().orElse(0);
        List<WorkloadResponse.Warning> warnings = new ArrayList<>();
        for (WorkloadResponse.MemberLoad load : loads) {
            if ("OVERLOADED".equals(load.level())) {
                warnings.add(new WorkloadResponse.Warning(load.accountId(), "OVERLOADED"));
            } else if (average > 0 && load.score() >= average * 2) {
                warnings.add(new WorkloadResponse.Warning(load.accountId(), "DOUBLE_OF_AVERAGE"));
            }
        }
        return new WorkloadResponse(loads, average, warnings);
    }

    @Override
    public SprintResponse startSprint(Long accountId, Long roomId, SprintStartCommand command) {
        access.requireMember(roomId, accountId);
        if (sprintRepository.findFirstByRoomIdAndStatus(roomId, SprintStatus.IN_PROGRESS).isPresent()) {
            throw new BusinessException(TaskErrorCode.SPRINT_ALREADY_IN_PROGRESS);
        }
        if (command.endDate().isBefore(command.startDate())) {
            throw new BusinessException(TaskErrorCode.SPRINT_PERIOD_INVALID);
        }
        Sprint saved = sprintRepository.save(Sprint.builder().roomId(roomId).sprintNo(sprintRepository.findMaxSprintNo(roomId) + 1).name(command.name()).startDate(command.startDate()).endDate(command.endDate()).status(SprintStatus.IN_PROGRESS).startedByAccountId(accountId).build());
        SprintResponse response = toResponse(saved);
        realtime.toRoom(roomId, "SPRINT_STARTED", response, accountId);
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public SprintResponse getCurrentSprint(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        return sprintRepository.findFirstByRoomIdAndStatus(roomId, SprintStatus.IN_PROGRESS).map(this::toResponse).orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<SprintResponse> getClosedSprints(Long accountId, Long roomId, Pageable pageable) {
        access.requireMember(roomId, accountId);
        return Pages.response(sprintRepository.findByRoomIdAndStatus(roomId, SprintStatus.CLOSED, pageable).map(this::toResponse));
    }

    @Override
    public SprintResponse updateSprint(Long accountId, Long roomId, Long sprintId, SprintUpdateCommand command) {
        access.requireMember(roomId, accountId);
        Sprint sprint = openSprint(roomId, sprintId);
        LocalDate end = command.endDate() != null ? command.endDate() : sprint.getEndDate();
        if (end.isBefore(sprint.getStartDate())) {
            throw new BusinessException(TaskErrorCode.SPRINT_PERIOD_INVALID);
        }
        SprintResponse response = toResponse(sprintRepository.save(sprint.toBuilder().name(command.name() != null ? command.name() : sprint.getName()).endDate(end).build()));
        realtime.toRoom(roomId, "SPRINT_UPDATED", response, accountId);
        return response;
    }

    @Override
    public SprintResponse closeSprint(Long accountId, Long roomId, Long sprintId) {
        access.requireMember(roomId, accountId);
        return toResponse(sprintCloser.close(openSprint(roomId, sprintId), accountId));
    }

    private Sprint openSprint(Long roomId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId).filter(s -> s.getRoomId().equals(roomId)).orElseThrow(() -> new BusinessException(TaskErrorCode.SPRINT_NOT_FOUND));
        if (sprint.getStatus() == SprintStatus.CLOSED) {
            throw new BusinessException(TaskErrorCode.SPRINT_ALREADY_CLOSED);
        }
        return sprint;
    }

    private SprintResponse toResponse(Sprint s) {
        Integer rate = s.getTotalTaskCount() == null || s.getTotalTaskCount() == 0 ? null : s.getDoneTaskCount() * 100 / s.getTotalTaskCount();
        return new SprintResponse(s.getId(), s.getSprintNo(), s.getName(), s.getStartDate(), s.getEndDate(), s.getStatus(), access.memberRef(s.getRoomId(), s.getStartedByAccountId()), s.getClosedAt(), s.getTotalTaskCount(), s.getDoneTaskCount(), rate);
    }

    private double weight(TaskPriority priority) {
        return switch (priority) {
            case HIGH -> 1.5;
            case NORMAL -> 1;
            case LOW -> 0.5;
        };
    }

    private IdeaCard requireConvertibleCard(Long roomId, Long cardId) {
        IdeaCard card = cardRepository.findById(cardId).filter(c -> c.getRoomId().equals(roomId) && c.getDeletedAt() == null).orElseThrow(() -> new BusinessException(IdeaCardErrorCode.CARD_NOT_FOUND));
        if (taskRepository.existsBySourceCardIdAndDeletedAtIsNull(cardId)) {
            throw new BusinessException(IdeaCardErrorCode.CARD_ALREADY_CONVERTED);
        }
        return card;
    }

    private void requireFutureDue(LocalDate dueDate) {
        if (dueDate != null && dueDate.isBefore(KstTime.todayKst())) {
            throw new BusinessException(TaskErrorCode.TASK_DUE_DATE_IN_PAST);
        }
    }

    private void requireEditor(Long roomId, Task task, Long accountId) {
        if (!accountId.equals(task.getRegistrantAccountId()) && !accountId.equals(task.getAssigneeAccountId()) && !access.isOwner(roomId, accountId)) {
            throw new BusinessException(TaskErrorCode.TASK_NO_PERMISSION);
        }
    }

    private Task find(Long roomId, Long taskId) {
        return taskRepository.findById(taskId).filter(t -> t.getRoomId().equals(roomId) && t.getDeletedAt() == null).orElseThrow(() -> new BusinessException(TaskErrorCode.TASK_NOT_FOUND));
    }

    private void history(Long taskId, TaskHistoryType type, TaskStatus from, TaskStatus to, Long fromAssignee, Long toAssignee, Long actor) {
        historyRepository.save(TaskHistory.builder().taskId(taskId).historyType(type).fromStatus(from == null ? null : from.name()).toStatus(to == null ? null : to.name()).fromAssigneeId(fromAssignee).toAssigneeId(toAssignee).actorAccountId(actor).build());
    }

    private List<Task> reorder(List<Task> tasks) {
        List<Task> saved = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            saved.add(taskRepository.save(tasks.get(i).toBuilder().sortOrder(i).build()));
        }
        return saved;
    }

    private TaskDetailResponse broadcast(Long roomId, Long accountId, Task task, String type) {
        TaskDetailResponse response = detail(roomId, task);
        realtime.toRoom(roomId, type, response.summary(), accountId);
        return response;
    }

    private TaskDetailResponse detail(Long roomId, Task task) {
        List<TaskHistory> histories = historyRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());
        Set<Long> ids = new HashSet<>();
        histories.forEach(h -> {
            ids.add(h.getFromAssigneeId());
            ids.add(h.getToAssigneeId());
            ids.add(h.getActorAccountId());
        });
        Map<Long, MemberRef> refs = access.memberRefs(roomId, ids);
        return new TaskDetailResponse(summaries(roomId, List.of(task)).getFirst(), task.getDescription(), task.getCompletedAt(),
                fileAttachSupport.responses(fileRepository.findByTaskIdOrderBySortOrderAsc(task.getId()).stream().map(TaskFile::getFileId).toList()),
                histories.stream().map(h -> new TaskDetailResponse.History(h.getHistoryType(), h.getFromStatus(), h.getToStatus(), refs.get(h.getFromAssigneeId()), refs.get(h.getToAssigneeId()), refs.get(h.getActorAccountId()), h.getCreatedAt())).toList(),
                task.getCreatedAt());
    }

    private List<TaskSummaryResponse> summaries(Long roomId, List<Task> tasks) {
        if (tasks.isEmpty()) {
            return List.of();
        }
        Set<Long> people = new HashSet<>();
        tasks.forEach(t -> {
            people.add(t.getRegistrantAccountId());
            people.add(t.getAssigneeAccountId());
        });
        Map<Long, IdeaCard> cards = cardRepository.findByIdIn(tasks.stream().map(Task::getSourceCardId).filter(Objects::nonNull).collect(Collectors.toSet())).stream().collect(Collectors.toMap(IdeaCard::getId, Function.identity()));
        cards.values().forEach(c -> people.add(c.getWriterAccountId()));
        Map<Long, MemberRef> refs = access.memberRefs(roomId, people);
        Map<Long, Long> files = fileRepository.findByTaskIdIn(tasks.stream().map(Task::getId).toList()).stream().collect(Collectors.groupingBy(TaskFile::getTaskId, Collectors.counting()));
        LocalDate today = KstTime.todayKst();
        return tasks.stream().map(t -> {
            Long dDay = t.getDueDate() == null ? null : ChronoUnit.DAYS.between(today, t.getDueDate());
            boolean overdue = dDay != null && dDay < 0 && t.getStatus() != TaskStatus.DONE;
            return new TaskSummaryResponse(t.getId(), t.getTitle(), t.getStatus(), t.getPriority(), t.getDueDate(), dDay, overdue, t.getSortOrder(), t.getVersion(), refs.get(t.getRegistrantAccountId()), refs.get(t.getAssigneeAccountId()), source(t, cards, refs), t.getSprintId(), files.getOrDefault(t.getId(), 0L));
        }).toList();
    }

    private TaskSummaryResponse.Source source(Task task, Map<Long, IdeaCard> cards, Map<Long, MemberRef> refs) {
        if (task.getSourceType() == TaskSourceType.CARD) {
            IdeaCard card = cards.get(task.getSourceCardId());
            if (card == null || card.getDeletedAt() != null) {
                return new TaskSummaryResponse.Source(TaskSourceType.CARD, task.getSourceCardId(), null, null, true);
            }
            return new TaskSummaryResponse.Source(TaskSourceType.CARD, card.getId(), card.getTitle(), refs.get(card.getWriterAccountId()).displayName(), false);
        }
        if (task.getSourceType() == TaskSourceType.VOTE) {
            return new TaskSummaryResponse.Source(TaskSourceType.VOTE, task.getSourceVoteId(), voteRepository.findById(task.getSourceVoteId() == null ? -1L : task.getSourceVoteId()).map(Vote::getTitle).orElse(null), null, task.getSourceVoteId() == null);
        }
        return new TaskSummaryResponse.Source(TaskSourceType.NONE, null, null, null, false);
    }
}
