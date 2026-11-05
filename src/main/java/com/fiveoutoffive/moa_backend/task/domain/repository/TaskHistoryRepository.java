package com.fiveoutoffive.moa_backend.task.domain.repository;

import com.fiveoutoffive.moa_backend.task.domain.model.TaskHistory;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface TaskHistoryRepository {

    TaskHistory save(TaskHistory taskHistory);

    Optional<TaskHistory> findById(Long id);
}
