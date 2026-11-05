package com.fiveoutoffive.moa_backend.file.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.file.domain.model.StoredFile;
import com.fiveoutoffive.moa_backend.file.infrastructure.persistence.StoredFileJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StoredFileMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    StoredFileJpaEntity toJpaEntity(StoredFile storedFile);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default StoredFile toDomain(StoredFileJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return StoredFile.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getOwnerAccountId(),
                jpaEntity.getRoomId(),
                jpaEntity.getFileGroup(),
                jpaEntity.getObjectKey(),
                jpaEntity.getOriginalName(),
                jpaEntity.getMimeType(),
                jpaEntity.getSizeBytes(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt()
        );
    }
}
