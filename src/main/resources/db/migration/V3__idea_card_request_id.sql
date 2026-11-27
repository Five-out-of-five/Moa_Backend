-- 카드 등록 응답을 못 받았을 때 같은 요청을 다시 보내도 카드가 하나만 생기게 한다(재시도 키).
-- 프론트가 처음 저장할 때 만든 UUID 와, 그 요청 본문의 해시(같은 키에 다른 본문이 오면 409)를 보관한다.
-- 예전 카드와 키 없이 만든 카드는 NULL 이다.
ALTER TABLE idea_card ADD COLUMN request_id UUID;
ALTER TABLE idea_card ADD COLUMN request_hash VARCHAR(64);

-- 같은 사람이 같은 키로 카드를 두 장 만들 수 없다. 서비스가 키 단위로 잠그고 먼저 찾지만, 마지막 방어선으로 둔다.
CREATE UNIQUE INDEX uk_idea_card_request ON idea_card (writer_account_id, request_id) WHERE request_id IS NOT NULL;

COMMENT ON COLUMN idea_card.request_id IS '카드 등록 재시도 키 (프론트가 만든 UUID, 작성자별 유일)';
COMMENT ON COLUMN idea_card.request_hash IS '재시도 키로 처음 등록한 요청 본문의 SHA-256 (같은 키에 다른 본문이면 거절)';
