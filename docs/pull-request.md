# PR 작성 규칙

사람이 올리든 AI 도구가 올리든 모든 PR 은 이 규칙을 따른다.

## 기본 규칙

| 항목 | 규칙 |
|---|---|
| 대상 브랜치 | 항상 `develop`. `main` 으로 올리지 않는다 |
| 작업 브랜치 | `develop` 에서 분기한 `feature/{작업명}`, `fix/{작업명}`, `refactor/{작업명}`, `docs/{작업명}` |
| 제목 | `[타입] 변경 요약`. 타입은 `Feature`, `Fix`, `Refactor`, `Docs` |
| 상태 | 리뷰할 수 있는 상태로 올린다. 작업 중이면 Draft |

## 본문 양식

본문은 [.github/PULL_REQUEST_TEMPLATE.md](../.github/PULL_REQUEST_TEMPLATE.md) 의 양식을 쓴다. 제목(`##`)의 이름과 순서를 바꾸지 않는다.

```markdown
## 설명
이번 PR 에서 구현한 내용과 이유

## 🔗 Issue
Closes #이슈번호

---

## 확인할 사항
- [ ] 리뷰어가 확인할 내용
```

| 섹션 | 쓰는 내용 |
|---|---|
| 설명 | 무엇을 왜 바꿨는지. 파일 목록을 나열하지 않고 동작 변화 위주로 쓴다 |
| Issue | `Closes #번호`. 관련 이슈가 없으면 `없음` |
| 확인할 사항 | 리뷰어가 볼 지점, 테스트 방법, 아래 해당 항목 |

템플릿의 안내 주석(`<!-- -->`)과 `설명 작성` 같은 자리표시 문구는 지우고 쓴다.

### 확인할 사항에 반드시 적을 것

해당하는 경우에만 적는다.

- 공유 파일 변경과 영향 범위 (`global/**`, `application*.yaml`, `build.gradle`, `docker-compose.yml`)
- `global/websocket/**` 변경: BE-3 확인 필요
- 다른 도메인 변경: 해당 담당자를 리뷰어로 지정
- Flyway 마이그레이션 추가와 버전 번호
- 새 환경변수 (`.env.example` 반영 여부)
- API 계약 변경과 프론트 영향
- 명세와 다르게 구현한 부분과 이유
- 검증한 것과 검증하지 못한 것

## 올리기 전 확인

1. `develop` 최신 내용을 반영하고 충돌을 해결한다.
2. `./gradlew test` 를 통과한다. 돌리지 못했으면 이유를 '확인할 사항'에 적는다.
3. `.env`, 키, 비밀번호, 빌드 산출물, 로그가 포함되지 않았는지 확인한다.

## 올리는 방법

GitHub CLI:

```bash
gh pr create --base develop --title "[Feature] 변경 요약" --body-file pr_body.md
```

`gh` 를 쓸 수 없으면 아래 주소에서 만든다. `{브랜치}` 만 바꾼다.

```
https://github.com/Five-out-of-five/Moa_Backend/compare/develop...{브랜치}?expand=1
```

## AI 도구로 올릴 때

- PR 을 만들기 전에 이 문서와 템플릿을 읽고 그대로 작성한다.
- 사용자가 대상 브랜치를 따로 지정하지 않으면 `develop` 으로 올린다.
- 실행해서 확인하지 않은 내용을 검증했다고 쓰지 않는다.
