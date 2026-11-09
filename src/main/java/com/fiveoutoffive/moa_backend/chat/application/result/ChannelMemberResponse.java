package com.fiveoutoffive.moa_backend.chat.application.result;

import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelMemberStatus;
import com.fiveoutoffive.moa_backend.global.infrastructure.s3.CdnMappable;

public record ChannelMemberResponse(Long accountId, String displayName, String profileImageUrl, ChannelMemberStatus status, boolean online) implements CdnMappable {
}
