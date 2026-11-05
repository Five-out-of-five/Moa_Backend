package com.fiveoutoffive.moa_backend.ai.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.ai.domain.model.ContentEmbedding;
import com.fiveoutoffive.moa_backend.ai.infrastructure.persistence.ContentEmbeddingJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContentEmbeddingMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    ContentEmbeddingJpaEntity toJpaEntity(ContentEmbedding contentEmbedding);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default ContentEmbedding toDomain(ContentEmbeddingJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return ContentEmbedding.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getCardId(),
                jpaEntity.getTaskId(),
                jpaEntity.getVoteId(),
                fromVectorLiteral(jpaEntity.getEmbedding()),
                jpaEntity.getModel(),
                jpaEntity.getSourceHash(),
                jpaEntity.getUpdatedAt()
        );
    }

    // float[] <-> pgvector 문자열('[0.1,0.2]') 변환. MapStruct 가 toJpaEntity 에서 자동으로 사용한다.
    default String toVectorLiteral(float[] vector) {
        if (vector == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.append(']').toString();
    }

    default float[] fromVectorLiteral(String literal) {
        if (literal == null || literal.length() < 2) {
            return null;
        }
        String body = literal.substring(1, literal.length() - 1).trim();
        if (body.isEmpty()) {
            return new float[0];
        }
        String[] parts = body.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        return vector;
    }
}
