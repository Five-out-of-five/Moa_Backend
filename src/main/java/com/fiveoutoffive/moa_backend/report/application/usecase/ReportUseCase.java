package com.fiveoutoffive.moa_backend.report.application.usecase;

import com.fiveoutoffive.moa_backend.report.application.command.ReportCreateCommand;
import com.fiveoutoffive.moa_backend.report.application.result.ReportResponse;

public interface ReportUseCase {

    ReportResponse create(Long accountId, ReportCreateCommand command);
}
