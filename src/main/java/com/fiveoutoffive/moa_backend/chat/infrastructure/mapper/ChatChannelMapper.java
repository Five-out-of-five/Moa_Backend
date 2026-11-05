package com.fiveoutoffive.moa_backend.chat.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannel;
import com.fiveoutoffive.moa_backend.chat.infrastructure.persistence.ChatChannelJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatChannelMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    ChatChannelJpaEntity toJpaEntity(ChatChannel chatChannel);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default ChatChannel toDomain(ChatChannelJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return ChatChannel.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getRoomId(),
                jpaEntity.getChannelType(),
                jpaEntity.getName(),
                jpaEntity.getIsDefault(),
                jpaEntity.getDmKey(),
                jpaEntity.getCreatorAccountId(),
                jpaEntity.getDeletedAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
