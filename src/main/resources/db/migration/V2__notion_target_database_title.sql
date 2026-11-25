-- 노션 연동 조회의 targetDatabase.title 을 채우기 위해, 설정을 저장할 때 확인한 데이터베이스 이름을 함께 보관한다.
-- 이미 저장된 연동은 설정을 다시 저장할 때 채워진다.
ALTER TABLE notion_integration ADD COLUMN target_database_title TEXT;

COMMENT ON COLUMN notion_integration.target_database_title IS '내보낼 노션 데이터베이스 이름 (설정을 저장한 시점)';
