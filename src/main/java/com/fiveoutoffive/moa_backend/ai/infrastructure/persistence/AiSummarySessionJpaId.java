package com.fiveoutoffive.moa_backend.ai.infrastructure.persistence;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** ai_summary_session 복합 PK (summary_id, session_id) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class AiSummarySessionJpaId implements Serializable {

    private Long summaryId;
    private Long sessionId;
}
