package com.fiveoutoffive.moa_backend.account.infrastructure.persistence;

import com.fiveoutoffive.moa_backend.account.domain.model.AccountRole;
import com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus;
import com.fiveoutoffive.moa_backend.account.domain.model.SocialProvider;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, Long> {

    Optional<AccountJpaEntity> findByProviderAndProviderUid(SocialProvider provider, String providerUid);

    boolean existsByEmail(String email);

    long countByRoleAndDeletedAtIsNull(AccountRole role);

    List<AccountJpaEntity> findByIdIn(Collection<Long> ids);

    List<AccountJpaEntity> findByPurgeAtBeforeAndEmailIsNotNull(LocalDateTime time);

    List<AccountJpaEntity> findByStatusAndSuspendedUntilBefore(AccountStatus status, LocalDateTime time);

    @Query("select a from AccountJpaEntity a where (cast(:keyword as string) is null or lower(a.name) like lower(concat('%', cast(:keyword as string), '%')) or lower(a.email) like lower(concat('%', cast(:keyword as string), '%'))) and (:provider is null or a.provider = :provider) and (:status is null or a.status = :status) and (:role is null or a.role = :role) and (cast(:joinedFrom as timestamp) is null or a.createdAt >= :joinedFrom) and (cast(:joinedTo as timestamp) is null or a.createdAt < :joinedTo)")
    Page<AccountJpaEntity> search(@Param("keyword") String keyword, @Param("provider") SocialProvider provider, @Param("status") AccountStatus status, @Param("role") AccountRole role, @Param("joinedFrom") LocalDateTime joinedFrom, @Param("joinedTo") LocalDateTime joinedTo, Pageable pageable);

    long countByCreatedAtBefore(LocalDateTime time);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);

    @Query("select a.id from AccountJpaEntity a where a.status <> com.fiveoutoffive.moa_backend.account.domain.model.AccountStatus.WITHDRAWN")
    List<Long> findActiveIds();
}
