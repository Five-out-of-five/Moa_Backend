package com.fiveoutoffive.moa_backend.ideacard.application.result;

import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import java.util.List;

public record ReactionDetailResponse(String emoji, long count, List<MemberRef> users) {
}
