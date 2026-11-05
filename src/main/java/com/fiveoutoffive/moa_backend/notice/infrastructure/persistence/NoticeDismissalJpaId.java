package com.fiveoutoffive.moa_backend.notice.infrastructure.persistence;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** notice_dismissal 복합 PK (notice_id, account_id) */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class NoticeDismissalJpaId implements Serializable {

    private Long noticeId;
    private Long accountId;
}
