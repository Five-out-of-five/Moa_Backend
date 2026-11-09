package com.fiveoutoffive.moa_backend.mood.presentation.api;

import com.fiveoutoffive.moa_backend.global.annotation.LoginAccount;
import com.fiveoutoffive.moa_backend.global.common.api.response.ApiResponse;
import com.fiveoutoffive.moa_backend.mood.application.result.MoodResponse;
import com.fiveoutoffive.moa_backend.mood.application.result.TeamMoodResponse;
import com.fiveoutoffive.moa_backend.mood.application.usecase.MoodUseCase;
import com.fiveoutoffive.moa_backend.mood.presentation.api.request.MoodUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/moods")
@RequiredArgsConstructor
@Tag(name = "06. Mood", description = "상태값")
public class MoodController {

    private final MoodUseCase useCase;

    @PutMapping("/me")
    @Operation(summary = "오늘 상태값 등록·변경")
    public ApiResponse<MoodResponse> upsert(@LoginAccount Long accountId, @PathVariable Long roomId, @Valid @RequestBody MoodUpsertRequest request) {
        return ApiResponse.success(useCase.upsertMine(accountId, roomId, request.toCommand()));
    }

    @GetMapping
    @Operation(summary = "팀 상태값 현황")
    public ApiResponse<TeamMoodResponse> team(@LoginAccount Long accountId, @PathVariable Long roomId, @RequestParam(defaultValue = "TODAY") String period) {
        return ApiResponse.success(useCase.getTeam(accountId, roomId, period));
    }
}
