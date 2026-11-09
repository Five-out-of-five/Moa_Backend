package com.fiveoutoffive.moa_backend.briefing.application.service;

import com.fiveoutoffive.moa_backend.briefing.application.result.BriefingResponse;
import java.util.List;
import java.util.Map;

/** 브리핑 '내가 할 일'의 원천. 업무·투표 도메인이 각자 구현한다. */
public interface MyTodoReader {

    /** key: assignedTasks / returnedTasks / unassignedTasks / unvotedVotes */
    Map<String, List<BriefingResponse.TodoItem>> read(Long roomId, Long accountId);
}
