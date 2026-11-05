package com.fiveoutoffive.moa_backend.task.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   GET    /api/v1/rooms/{roomId}/tasks  업무 보드 목록 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/tasks  업무 등록 (카드·투표에서 전환 포함) [방 멤버]
//   GET    /api/v1/rooms/{roomId}/tasks/{taskId}  업무 상세 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/tasks/{taskId}  업무 내용 수정 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/tasks/{taskId}/status  업무 상태 변경·칸반 이동 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/tasks/{taskId}/sprint  업무 스프린트 이동 [방 멤버]
//   PATCH  /api/v1/rooms/{roomId}/tasks/{taskId}/assignee  담당자 변경 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/tasks/{taskId}/reject  업무 반려 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/tasks/{taskId}  업무 삭제 [방 멤버]
//   POST   /api/v1/rooms/{roomId}/tasks/{taskId}/files  업무 첨부 추가 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/tasks/{taskId}/files/{fileId}  업무 첨부 삭제 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "09. Task", description = "업무·스프린트")
public class TaskController {
}
