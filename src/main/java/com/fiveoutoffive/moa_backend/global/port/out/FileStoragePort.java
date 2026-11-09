package com.fiveoutoffive.moa_backend.global.port.out;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 스토리지 통신을 위한 공통 아웃바운드 포트.
 *
 * <p>버킷은 전역 단일 버킷을 사용하므로 파라미터로 받지 않는다.
 * 업로드는 저장된 object key(상대경로)를 반환하고, 삭제는 그 key를 그대로 받는다.
 * (응답 시 CDN 루트를 붙여 절대 URL로 변환)
 *
 * <p>application 계층은 이 인터페이스에만 의존하고, 구현체는 {@code infrastructure/s3}가 제공한다.
 */
public interface FileStoragePort {

    /** 파일을 업로드하고 저장된 object key(상대경로, 예: {@code examples/uuid.png})를 반환한다. */
    String uploadFile(MultipartFile file, String directory);

    /** object key(상대경로)로 파일을 삭제한다. */
    void deleteFile(String key);

    /**
     * 짧은 유효기간의 서명된 다운로드 URL 을 만든다.
     *
     * @param downloadName 내려받을 때의 파일명. null 이면 브라우저에서 바로 연다 (미리보기용).
     */
    String createDownloadUrl(String key, String downloadName, java.time.Duration ttl);

    /** 로컬 파일을 지정한 key로 업로드하고 그 key를 반환한다. (비동기/배치 업로드용) */
    String uploadFile(java.io.File file, String targetKey);
}
