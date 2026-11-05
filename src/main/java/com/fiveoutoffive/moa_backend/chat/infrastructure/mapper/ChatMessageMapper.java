package com.fiveoutoffive.moa_backend.chat.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessage;
import com.fiveoutoffive.moa_backend.chat.infrastructure.persistence.ChatMessageJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMessageMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    ChatMessageJpaEntity toJpaEntity(ChatMessage chatMessage);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default ChatMessage toDomain(ChatMessageJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return ChatMessage.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getChannelId(),
                jpaEntity.getSenderAccountId(),
                jpaEntity.getMessageType(),
                jpaEntity.getContent(),
                jpaEntity.getEditedAt(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt()
        );
    }
}
