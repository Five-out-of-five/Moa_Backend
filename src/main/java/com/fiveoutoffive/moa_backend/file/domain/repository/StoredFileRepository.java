package com.fiveoutoffive.moa_backend.file.domain.repository;

import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface StoredFileRepository {

    StoredFile save(StoredFile storedFile);

    Optional<StoredFile> findById(Long id);

    List<StoredFile> findByIdIn(Collection<Long> ids);

    List<StoredFile> findByRoomId(Long roomId);

    long sumSizeByRoomId(@Param("roomId") Long roomId);

    void deleteById(Long id);

    List<StoredFile> findUnattachedBefore(@Param("before") LocalDateTime before);

    boolean isAttached(@Param("id") Long id);

    Page<Object[]> searchRoomFiles(@Param("roomId") Long roomId, @Param("fileGroup") String fileGroup, @Param("uploaderId") Long uploaderId, @Param("keyword") String keyword, @Param("mimeLike") String mimeLike, Pageable pageable);
}
