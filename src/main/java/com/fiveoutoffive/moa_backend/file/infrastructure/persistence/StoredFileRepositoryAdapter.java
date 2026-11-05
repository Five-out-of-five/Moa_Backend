package com.fiveoutoffive.moa_backend.file.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.file.infrastructure.mapper.StoredFileMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
}
