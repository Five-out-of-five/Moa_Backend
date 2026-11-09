package com.fiveoutoffive.moa_backend.admin.application.result;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StatsOverviewResponse(Totals totals, List<Point> series, LocalDateTime aggregatedAt) {

    public record Totals(long accounts, long rooms, long messages) {
    }

    public record Point(LocalDate periodStart, long newAccounts, long activeAccounts, long newRooms, long messages) {
    }
}
