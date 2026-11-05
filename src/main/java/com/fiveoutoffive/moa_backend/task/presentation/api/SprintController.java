package com.fiveoutoffive.moa_backend.task.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   POST   /api/v1/rooms/{roomId}/sprints  스프린트 시작 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/sprints/current  진행 중 스프린트 조회 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/sprints  지난 스프린트 목록 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/sprints/{sprintId}  스프린트 수정 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/sprints/{sprintId}/close  스프린트 마감 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "09. Task", description = "업무·스프린트")
public class SprintController {
}
