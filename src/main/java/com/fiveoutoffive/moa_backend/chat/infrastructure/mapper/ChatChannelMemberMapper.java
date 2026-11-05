package com.fiveoutoffive.moa_backend.chat.infrastructure.mapper;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannelMember;
import com.fiveoutoffive.moa_backend.chat.infrastructure.persistence.ChatChannelMemberJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatChannelMemberMapper {

    // 도메인 -> JPA 엔티티 (JPA 엔티티의 생성자로 자동 매핑)
    ChatChannelMemberJpaEntity toJpaEntity(ChatChannelMember chatChannelMember);

    // JPA 엔티티 -> 도메인 (도메인은 생성자가 닫혀 있으므로 reconstitute 로 복원)
    default ChatChannelMember toDomain(ChatChannelMemberJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return ChatChannelMember.reconstitute(
                jpaEntity.getId(),
                jpaEntity.getChannelId(),
                jpaEntity.getAccountId(),
                jpaEntity.getStatus(),
                jpaEntity.getLastReadMessageId(),
                jpaEntity.getHiddenAt(),
                jpaEntity.getJoinedAt(),
                jpaEntity.getLeftAt(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }
}
