package com.fiveoutoffive.moa_backend.task.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/tasks/progress  진척도 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/tasks/workload  업무 밸런서 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "09. Task", description = "업무·스프린트")
public class TaskInsightController {
}
