package com.fiveoutoffive.moa_backend.chat.domain.model;

/** 초대됨 / 참여 / 나감 / 내보내짐 (chat_channel_member.status) */
public enum ChannelMemberStatus {
    INVITED,
    JOINED,
    LEFT,
    KICKED
}
