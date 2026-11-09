package com.fiveoutoffive.moa_backend.global.infrastructure.s3;

import com.fiveoutoffive.moa_backend.global.exception.RequiredPropertyMissingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * S3 호환 스토리지 설정. AWS S3 와 MinIO 를 같은 코드로 쓴다.
 *
 * <p>단일 버킷을 사용하며, 도메인 구분은 버킷이 아니라 object key의 prefix로 표현한다.
 * {@code S3_ENDPOINT} 를 주면 그 주소(path-style)로 접속하고, 비우면 SDK 가 리전으로 AWS 엔드포인트를 결정한다.
 *
 * <p>DB에는 object key(상대경로)만 저장하고, 응답 시 {@link #getCdnBase()}를 앞에 붙여 절대 URL을 만든다.
 * CDN(CloudFront 등)을 쓰면 {@code S3_CDN_URL}로 응답 URL 루트만 바꿔주면 되고 저장된 데이터는 건드릴 필요가 없다.
 */
@Slf4j
@Getter
@Component
public class S3Settings {

    /** 단일 버킷명. 필수값이다. */
    private final String bucket;

    /** AWS 리전. 클라이언트 리전과 URL 조합에 함께 쓰인다. */
    private final String region;

    /** 응답 URL 조합용 루트. key 앞에 붙는다. (trailing slash 제거) */
    private final String cdnBase;

    /** S3 호환 스토리지 주소. AWS 면 빈 값이다. */
    private final String endpoint;

    /** 브라우저가 접근하는 스토리지 주소. */
    private final String publicEndpoint;

    public S3Settings(
            @Value("${cloud.aws.s3.bucket:}") String bucket,
            @Value("${cloud.aws.region.static:ap-northeast-2}") String region,
            @Value("${cloud.aws.s3.cdn-url:}") String cdnOverride,
            @Value("${cloud.aws.s3.endpoint:}") String endpoint,
            @Value("${cloud.aws.s3.public-endpoint:}") String publicEndpoint
    ) {
        this.endpoint = trimSlash(endpoint);
        this.publicEndpoint = publicEndpoint == null || publicEndpoint.isBlank() ? this.endpoint : trimSlash(publicEndpoint);
        // 환경변수에 실수로 붙은 앞뒤 공백/개행이 버킷명·리전에 섞이면
        // AWS가 잘못된 버킷/호스트로 인식해 업로드가 500으로 실패하므로 방어적으로 trim 한다.
        this.bucket = bucket == null ? "" : bucket.trim();
        this.region = region == null ? "" : region.trim();

        if (this.bucket.isBlank()) {
            throw new RequiredPropertyMissingException(
                    "cloud.aws.s3.bucket",
                    "S3_BUCKET",
                    "S3 버킷명이 비어 있습니다.",
                    """
                    아래 중 하나를 선택하세요.

                      1) 파일 업로드를 사용한다면 버킷명을 주입합니다.
                         export S3_BUCKET=your-bucket-name
                         (IntelliJ는 Run/Debug Configurations > Environment variables 에 추가)

                      2) 아직 업로드 기능이 필요 없다면 임시 값으로 넘길 수 있습니다.
                         export S3_BUCKET=local-dev-placeholder
                         버킷 접근 확인에 실패해도 기동은 되며 [S3] 경고 로그만 남습니다.

                      3) 업로드 기능을 아예 쓰지 않는다면 아래를 삭제하세요.
                         global/infrastructure/s3, global/port/out,
                         global/config/S3Config.java, global/config/CdnJacksonConfig.java,
                         global/type/FileType.java, global/util/FileTypeDetector.java,
                         example/settings/ExampleStorageSettings.java,
                         application.yaml의 cloud.aws 블록""");
        }

        this.cdnBase = resolveCdnBase(cdnOverride);

        log.info("[S3Settings] bucket={}, region={}, cdnBase={}", this.bucket, this.region, this.cdnBase);
    }

    private String resolveCdnBase(String cdnOverride) {
        // 직접 지정한 CDN 루트를 최우선으로 사용 (예: CloudFront 도메인)
        if (cdnOverride != null && !cdnOverride.isBlank()) {
            return trimSlash(cdnOverride);
        }
        // MinIO 는 path-style: {endpoint}/{bucket}
        if (usesCustomEndpoint()) {
            return publicEndpoint + "/" + bucket;
        }
        // 기본은 virtual-hosted 형식: https://{bucket}.s3.{region}.amazonaws.com
        return "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    public boolean usesCustomEndpoint() {
        return !endpoint.isBlank();
    }

    private static String trimSlash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
