package com.fiveoutoffive.moa_backend.file.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataStoredFileRepository extends JpaRepository<StoredFileJpaEntity, Long> {

    List<StoredFileJpaEntity> findByIdIn(Collection<Long> ids);

    List<StoredFileJpaEntity> findByRoomId(Long roomId);

    @Query("select coalesce(sum(f.sizeBytes), 0) from StoredFileJpaEntity f where f.roomId = :roomId and f.deletedAt is null")
    long sumSizeByRoomId(@Param("roomId") Long roomId);

    void deleteById(Long id);

    @Query(value = "select f.* from file f where f.created_at < :before and f.deleted_at is null and f.file_group in ('CHAT_ATTACHMENT','CARD_ATTACHMENT','TASK_ATTACHMENT') and not exists (select 1 from card_file c where c.file_id = f.id) and not exists (select 1 from task_file t where t.file_id = f.id) and not exists (select 1 from chat_message_file m where m.file_id = f.id)", nativeQuery = true)
    List<StoredFileJpaEntity> findUnattachedBefore(@Param("before") LocalDateTime before);

    @Query(value = "select exists (select 1 from card_file c where c.file_id = :id) or exists (select 1 from task_file t where t.file_id = :id) or exists (select 1 from chat_message_file m where m.file_id = :id)", nativeQuery = true)
    boolean isAttached(@Param("id") Long id);

    @Query(value = "select f.id, f.original_name, f.mime_type, f.size_bytes, f.owner_account_id, f.created_at, f.file_group, coalesce(c.card_id, t.task_id, m.message_id) as source_id from file f left join card_file c on c.file_id = f.id left join idea_card ic on ic.id = c.card_id left join task_file t on t.file_id = f.id left join task tk on tk.id = t.task_id left join chat_message_file m on m.file_id = f.id left join chat_message cm on cm.id = m.message_id where f.room_id = :roomId and f.deleted_at is null and f.file_group in ('CHAT_ATTACHMENT','CARD_ATTACHMENT','TASK_ATTACHMENT') and (c.id is not null and ic.deleted_at is null or t.id is not null and tk.deleted_at is null or m.id is not null and cm.deleted_at is null) and (:fileGroup is null or f.file_group = :fileGroup) and (:uploaderId is null or f.owner_account_id = :uploaderId) and (:keyword is null or lower(f.original_name) like lower('%' || :keyword || '%')) and (:mimeLike is null or f.mime_type like :mimeLike) order by f.created_at desc", countQuery = "select count(*) from file f where f.room_id = :roomId and f.deleted_at is null and f.file_group in ('CHAT_ATTACHMENT','CARD_ATTACHMENT','TASK_ATTACHMENT') and (:fileGroup is null or f.file_group = :fileGroup) and (:uploaderId is null or f.owner_account_id = :uploaderId) and (:keyword is null or lower(f.original_name) like lower('%' || :keyword || '%')) and (:mimeLike is null or f.mime_type like :mimeLike)", nativeQuery = true)
    Page<Object[]> searchRoomFiles(@Param("roomId") Long roomId, @Param("fileGroup") String fileGroup, @Param("uploaderId") Long uploaderId, @Param("keyword") String keyword, @Param("mimeLike") String mimeLike, Pageable pageable);
}
