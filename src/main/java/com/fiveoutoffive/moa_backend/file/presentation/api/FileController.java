package com.fiveoutoffive.moa_backend.file.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   POST   /api/v1/rooms/{roomId}/files  파일 업로드 [방 멤버]
//   DELETE /api/v1/rooms/{roomId}/files/{fileId}  업로드 취소 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/files  방 파일 목록 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/files/{fileId}/download-url  다운로드 URL 발급 [방 멤버]
//   GET    /api/v1/rooms/{roomId}/storage-usage  방 저장 용량 조회 [방 멤버]
@RestController
@RequiredArgsConstructor
@Tag(name = "03. File", description = "파일")
public class FileController {
}
