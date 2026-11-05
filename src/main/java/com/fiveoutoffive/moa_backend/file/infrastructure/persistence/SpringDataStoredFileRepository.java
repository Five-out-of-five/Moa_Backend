package com.fiveoutoffive.moa_backend.file.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStoredFileRepository extends JpaRepository<StoredFileJpaEntity, Long> {
}
