package com.fiveoutoffive.moa_backend.task.domain.repository;

import com.fiveoutoffive.moa_backend.task.domain.model.Sprint;
import com.fiveoutoffive.moa_backend.task.domain.model.SprintStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface SprintRepository {

    Sprint save(Sprint sprint);

    Optional<Sprint> findById(Long id);

    Optional<Sprint> findFirstByRoomIdAndStatus(Long roomId, SprintStatus status);

    Page<Sprint> findByRoomIdAndStatus(Long roomId, SprintStatus status, Pageable pageable);

    List<Sprint> findByStatusAndEndDateBefore(SprintStatus status, LocalDate date);

    int findMaxSprintNo(@Param("roomId") Long roomId);
}
