package com.fiveoutoffive.moa_backend.chat.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessageFile;
import com.fiveoutoffive.moa_backend.chat.infrastructure.persistence.ChatMessageFileJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMessageFileMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    ChatMessageFileJpaEntity toJpaEntity(ChatMessageFile chatMessageFile);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default ChatMessageFile toDomain(ChatMessageFileJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return ChatMessageFile.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getMessageId(),
                jpaEntity.getFileId(),
                jpaEntity.getSortOrder()
        );
    }
}
