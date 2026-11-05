package com.fiveoutoffive.moa_backend.report.presentation.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

// 담당 API (모아_API명세 기준). 구현할 때 엔드포인트를 이 클래스에 추가한다.
//   POST   /api/v1/reports  신고 접수 [로그인]
@RestController
@RequiredArgsConstructor
@Tag(name = "17. Report", description = "신고")
public class ReportController {
}
