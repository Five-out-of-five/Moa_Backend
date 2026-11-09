package com.fiveoutoffive.moa_backend.account.domain.repository;

import com.fiveoutoffive.moa_backend.account.domain.model.Account;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

// Application, Domain 계층이 사용할 레포지토리 포트. 조회 메서드는 기능을 만들면서 추가한다.
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> findByProviderAndProviderUid(SocialProvider provider, String providerUid);

    boolean existsByEmail(String email);

    long countByRoleAndDeletedAtIsNull(AccountRole role);

    List<Account> findByIdIn(Collection<Long> ids);

    List<Account> findByPurgeAtBeforeAndEmailIsNotNull(LocalDateTime time);

    List<Account> findByStatusAndSuspendedUntilBefore(AccountStatus status, LocalDateTime time);

    Page<Account> search(@Param("keyword") String keyword, @Param("provider") SocialProvider provider, @Param("status") AccountStatus status, @Param("role") AccountRole role, @Param("joinedFrom") LocalDateTime joinedFrom, @Param("joinedTo") LocalDateTime joinedTo, Pageable pageable);

    long countByCreatedAtBefore(LocalDateTime time);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);

    List<Long> findActiveIds();
}
