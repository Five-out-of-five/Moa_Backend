package com.fiveoutoffive.moa_backend.ideacard.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 카드 첨부 파일 (card_file). 컬럼 정의는 moa_schema.sql 과 같아야 한다(ddl-auto: validate). */
@Entity
@Table(name = "card_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardFileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // 도메인 모델의 값을 받아 JPA 엔티티를 만든다 (MapStruct 가 사용)
    public CardFileJpaEntity(
            Long id,
            Long cardId,
            Long fileId,
            Integer sortOrder
    ) {
        this.id = id;
        this.cardId = cardId;
        this.fileId = fileId;
        this.sortOrder = sortOrder;
    }
}
