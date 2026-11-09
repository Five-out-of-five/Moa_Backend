package com.fiveoutoffive.moa_backend.account.application.result;

import java.util.List;

public record WithdrawalCheckResponse(boolean withdrawable, List<OwnedRoom> ownedRooms) {

    public record OwnedRoom(Long roomId, String name, long memberCount) {
    }
}
