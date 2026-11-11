package com.fiveoutoffive.moa_backend.file;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fiveoutoffive.moa_backend.file.application.service.FileAttachSupport;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.file.domain.repository.StoredFileRepository;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.exception.GlobalErrorCode;
import com.fiveoutoffive.moa_backend.global.port.out.FileStoragePort;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 첨부 파일 공통 검증. DB 없이 돌도록 저장소를 목으로 둔다. */
class FileAttachSupportTest {

    private final StoredFileRepository storedFileRepository = mock(StoredFileRepository.class);
    private final FileStoragePort fileStoragePort = mock(FileStoragePort.class);
    private final FileAttachSupport fileAttachSupport = new FileAttachSupport(storedFileRepository, fileStoragePort);

    @Test
    @DisplayName("같은 파일 ID 가 두 번 오면 400 COMMON_INVALID_INPUT 이고 파일을 조회하지 않는다")
    void duplicateFileIdsRejected() {
        assertThatThrownBy(() -> fileAttachSupport.requireAttachable(1L, 1L, List.of(12L, 12L), FileGroup.CARD_ATTACHMENT, 0))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getErrorCode()).isEqualTo(GlobalErrorCode.INVALID_REQUEST));
        verifyNoInteractions(storedFileRepository);
    }

    @Test
    @DisplayName("중복이 있으면 개수 한도보다 먼저 중복으로 거절한다")
    void duplicateCheckedBeforeLimit() {
        assertThatThrownBy(() -> fileAttachSupport.requireAttachable(1L, 1L, List.of(1L, 2L, 2L), FileGroup.CARD_ATTACHMENT, 4))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getErrorCode()).isEqualTo(GlobalErrorCode.INVALID_REQUEST));
    }

    @Test
    @DisplayName("빈 목록은 통과한다")
    void emptyListPasses() {
        assertThatCode(() -> fileAttachSupport.requireAttachable(1L, 1L, List.of(), FileGroup.CARD_ATTACHMENT, 0))
                .doesNotThrowAnyException();
    }
}
