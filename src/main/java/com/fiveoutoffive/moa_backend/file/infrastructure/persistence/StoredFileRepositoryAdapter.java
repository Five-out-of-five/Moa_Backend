package com.fiveoutoffive.moa_backend.file.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.file.infrastructure.mapper.StoredFileMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoredFileRepositoryAdapter implements StoredFileRepository {

    private final SpringDataStoredFileRepository springDataRepository;
    private final StoredFileMapper storedFileMapper;

    @Override
    public StoredFile save(StoredFile storedFile) {
        StoredFileJpaEntity saved = springDataRepository.save(storedFileMapper.toJpaEntity(storedFile));
        return storedFileMapper.toDomain(saved);
    }

    @Override
    public Optional<StoredFile> findById(Long id) {
        return springDataRepository.findById(id)
                .map(storedFileMapper::toDomain);
    }

    @Override
    public List<StoredFile> findByIdIn(Collection<Long> ids) {
        return springDataRepository.findByIdIn(ids).stream().map(storedFileMapper::toDomain).toList();
    }

    @Override
    public List<StoredFile> findByRoomId(Long roomId) {
        return springDataRepository.findByRoomId(roomId).stream().map(storedFileMapper::toDomain).toList();
    }

    @Override
    public long sumSizeByRoomId(@Param("roomId") Long roomId) {
        return springDataRepository.sumSizeByRoomId(roomId);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public List<StoredFile> findUnattachedBefore(@Param("before") LocalDateTime before) {
        return springDataRepository.findUnattachedBefore(before).stream().map(storedFileMapper::toDomain).toList();
    }

    @Override
    public boolean isAttached(@Param("id") Long id) {
        return springDataRepository.isAttached(id);
    }

    @Override
    public Page<Object[]> searchRoomFiles(@Param("roomId") Long roomId, @Param("fileGroup") String fileGroup, @Param("uploaderId") Long uploaderId, @Param("keyword") String keyword, @Param("mimeLike") String mimeLike, Pageable pageable) {
        return springDataRepository.searchRoomFiles(roomId, fileGroup, uploaderId, keyword, mimeLike, pageable);
    }
}
