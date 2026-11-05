package com.fiveoutoffive.moa_backend.mediation.domain.repository;

import com.fiveoutoffive.moa_backend.mediation.domain.model.CompromiseDraft;
import java.util.Optional;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface CompromiseDraftRepository {

    CompromiseDraft save(CompromiseDraft compromiseDraft);

    Optional<CompromiseDraft> findById(Long id);
}
