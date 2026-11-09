# 코드 컨벤션과 폴더 구조

## 도메인 폴더 구조

각 도메인은 같은 구조를 쓴다. 의존 방향은 `presentation → application → domain ← infrastructure` 다.

```
{domain}/
├── presentation/
│   ├── api/                 컨트롤러, STOMP @MessageMapping 핸들러
│   │   └── request/         요청 DTO + Bean Validation, toCommand()
│   └── advice/              도메인별 @RestControllerAdvice
├── application/
│   ├── usecase/             컨트롤러가 의존하는 인터페이스
│   ├── service/             유스케이스 구현, 스케줄러, 이벤트 핸들러
│   ├── command/             서비스 입력 record
│   └── result/              서비스 출력 record = API 응답 DTO
├── domain/
│   ├── model/               순수 모델과 enum (JPA·Spring 없음)
│   ├── repository/          저장소 포트
│   └── event/               도메인 이벤트 record
├── infrastructure/
│   ├── persistence/         JpaEntity, RepositoryAdapter, SpringData 저장소
│   ├── mapper/              MapStruct (도메인 ↔ JPA)
│   └── adapter/             global 포트 구현
└── exception/               {Domain}ErrorCode (code = API 명세 문자열)
```

`global/` 은 도메인을 참조하지 않는다. 도메인 규칙이 필요하면 `global` 에 포트를 두고 도메인이 구현한다(`WebSocketUserPort`, `StompDestinationAuthorizer`, `AccountGuardPort`).

응답 DTO 는 `application/result` 에 명세 이름(`RoomDetailResponse`)으로 둔다. 컨트롤러가 그대로 반환하므로 안쪽 계층이 presentation 을 참조하지 않고 같은 record 를 두 번 만들지 않는다.

## 새 API 추가 순서

1. API 명세에서 경로, 권한, 에러 코드, 설명을 확인한다.
2. 필요한 조회를 `domain/repository` 포트 → `RepositoryAdapter` → `SpringData*Repository` 에 추가한다.
3. `application/command`, `application/result` 에 record 를 만든다.
4. 서비스 메서드를 작성하고 `usecase` 인터페이스에 선언한다.
5. `presentation/api/request` 와 컨트롤러 메서드를 추가한다.
6. 상태를 바꾸는 API 면 실시간 이벤트, 알림, 브리핑 기록이 명세에 있는지 확인한다.

## 코드 규칙

**이름**
- 컨트롤러 `{Domain}Controller`, 서비스 `{Domain}Service`, 유스케이스 `{Domain}UseCase`
- 요청 `{Action}Request`, 입력 `{Action}Command`, 응답 `{Name}Response`
- 커밋 전까지 Spring 기본 빈과 이름이 겹치지 않는지 본다. `TaskScheduler` 클래스는 Spring 의 `taskScheduler` 빈과 충돌해 기동이 실패했다.

**컨트롤러**
- 로직 없이 유스케이스 호출과 응답 감싸기만 한다.
- 로그인 사용자는 `@LoginAccount Long accountId` 로 받는다. JWT subject 는 계정 ID 다.
- 생성도 HTTP 200 이다(명세 규칙). `ApiResponse.success(data)`, 본문이 없으면 `ApiResponse.empty()`.
- 목록은 `Pages.of(page, size)`(최대 100) 와 `PageResponse`, 채팅·AI 이력은 `CursorResponse` 를 쓴다.

**서비스**
- 클래스에 `@Transactional`, 조회 메서드에 `@Transactional(readOnly = true)`.
- 방 API 는 첫 줄에서 `access.requireMember(roomId, accountId)` 또는 `requireOwner` 를 호출한다.
- 하위 자원은 소속 방을 확인하고, 다르면 404 로 존재를 숨긴다.
- 실패는 `throw new BusinessException({Domain}ErrorCode.X)` 로 던진다.

**도메인 모델**
- 생성은 `X.builder()`, 변경은 `x.toBuilder()...build()` 후 저장한다.
- 규칙이 여러 서비스에 반복되면 도메인 메서드로 옮긴다.

**JPA**
- 소프트 딜리트 테이블은 조회에 `deletedAt` 조건을 빠뜨리지 않는다.
- 목록 응답은 항목 수만큼 쿼리가 늘지 않게 `IN` 으로 한 번에 읽는다(`IdeaCardViewAssembler` 참고).
- `@Modifying`·`deleteBy…` 는 트랜잭션 안에서만 동작한다. `@Async` 스레드에는 트랜잭션이 없다.
- nullable 문자열 파라미터를 `lower()` 에 넣는 JPQL 은 `cast(:x as string)` 으로 타입을 지정한다.
- `@TransactionalEventListener` 에 트랜잭션이 필요하면 `Propagation.REQUIRES_NEW` 를 쓴다.

**동시 수정**: 카드·업무 이동과 투표 수정은 요청의 `version` 을 비교하고, 다르면 409 와 최신 상태를 `BusinessException` 의 data 로 보낸다.

**삭제 정책**
- 댓글: 행은 남기고 `deleted_at` 기록과 함께 본문을 즉시 지운다. 자리에는 '삭제된 댓글입니다'가 표시되며 원문은 신고 스냅샷에만 남는다.
- 메시지·카드·업무·투표: 소프트 딜리트. 리액션·알림: 행 삭제.

**주석**: 코드를 다시 설명하지 않고, 왜 그렇게 했는지만 한국어로 쓴다.
