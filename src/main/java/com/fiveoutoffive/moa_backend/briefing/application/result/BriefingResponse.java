package com.fiveoutoffive.moa_backend.briefing.application.result;

import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventTargetType;
import com.fiveoutoffive.moa_backend.briefing.domain.model.BoardEventType;
import java.time.LocalDateTime;
import java.util.List;

public record BriefingResponse(boolean visible, LocalDateTime lastCheckedAt, MyTodos myTodos, List<TeamNews> teamNews, Highlights highlights) {

    public record TodoItem(String targetType, Long targetId, String title) {
    }

    public record MyTodos(List<TodoItem> assignedTasks, List<TodoItem> returnedTasks, List<TodoItem> unassignedTasks, List<TodoItem> unvotedVotes) {
    }

    public record TeamNews(BoardEventType eventType, int count, List<NewsItem> items) {
    }

    public record NewsItem(Long eventId, BoardEventTargetType targetType, Long targetId, String title, String actorName, LocalDateTime createdAt) {
    }

    public record Highlights(List<NewsItem> commentsOnMyCards, List<NewsItem> myCardsConverted) {
    }
}
