# 계좌 이체 흐름 

계좌 이체는 멱등 처리 + outbox 패턴을 사용해
정확히 한 번의 송금과 최소 한 번의 이벤트 발행을 보장한다.

---

## 0. 데이터 모델 전제

모든 멱등/이벤트 데이터는 `integration` 스키마에서 관리한다.

### `integration.idempotency_requests`

- `(client_id, scope, idempotency_key)` UNIQUE
- `status`: `IN_PROGRESS | SUCCEEDED | FAILED`
- `request_hash`
- `response_snapshot` (transferId, status, error_code)
- `started_at`, `completed_at`

#### request_hash 규칙

- canonical string
  `fromAccountId=<long>|toAccountId=<long>|amount=<long>`
- 모든 값은 정수
- `SHA-256` 해시

---

### `integration.outbox_events`

- `event_id` (UUID, PK)
- `aggregate_type`: `TRANSFER`
- `aggregate_id`: transferId
- `event_type`: `TRANSFER_COMPLETED`
- `payload`
- `status`: `NEW | SENT`
- `created_at`

---

## 1. 클라이언트 요청

- `POST /idempotency-keys`로 짧은 수명(예: 10~30분)의 `Idempotency-Key` 발급
- 동일 요청 재시도 시 동일 키 사용
- 요청 바디

```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 10000
}
```

---

## 2. 서버 요청 수신

1. `Idempotency-Key` 존재 및 만료 여부 검증
2. `(client_id, scope, idempotency_key)` 기준으로 멱등 처리 시작

---

## 3. 멱등 레코드 선점 (트랜잭션 A)

### INSERT 시도

```sql
INSERT INTO idempotency_requests (...)
VALUES (..., IN_PROGRESS, request_hash, now())
```

---

### 기존 레코드 존재 시 처리

#### status = `SUCCEEDED`

- `response_snapshot` 그대로 반환
- 즉시 종료

#### status = `IN_PROGRESS`

- `started_at` 기준 timeout 검사

    - timeout 미초과 → `202 Accepted`

      ```json
      { "status": "IN_PROGRESS" }
      ```
    - timeout 초과 → 배치 대상 (요청 경로에서 상태 변경하지 않음)

#### status = `FAILED`

- FAILED 응답 그대로 반환
- 같은 멱등키 재시도 금지
- 재시도는 새 멱등키로만 허용

---

### request_hash 불일치

- 동일 멱등키 + 다른 요청 바디
- `409 Conflict`

---

## 4. 송금 처리 (트랜잭션 B)

아래 단계는 단일 DB 트랜잭션으로 수행한다.

1. 계좌 잠금

    - `min(accountId) → max(accountId)` 순서로 row lock
2. 잔액 검증

    - 부족 시 `FAILED`, `error_code = INSUFFICIENT_BALANCE`
3. 잔액 변경

    - from 감소 / to 증가
4. 원장 기록

    - transferId 생성
    - ledger insert
    - `transferId UNIQUE` (최종 중복 방어)
5. outbox 적재

    - `TRANSFER_COMPLETED`, `status = NEW`
6. 멱등 레코드 완료 처리

    - `status = SUCCEEDED`
    - `response_snapshot` 저장
7. 커밋

    - 실패 시 전체 롤백 + `FAILED`

---

## 5. 응답 반환

- `200 OK`

```json
{
  "transferId": "...",
  "status": "SUCCEEDED"
}
```

- 응답 전 서버 크래시 발생 시
  → 재요청에서 `response_snapshot`으로 동일 응답 반환

---

## 6. IN_PROGRESS → FAILED 배치(watchdog)

### 목적

- 고아 `IN_PROGRESS` 제거
- 장애 후 정합성 회복

### 동작

- 주기 실행(예: 1분)

```sql
UPDATE idempotency_requests
SET status = 'FAILED',
    completed_at = now(),
    error_code = 'TIMEOUT'
WHERE status = 'IN_PROGRESS'
  AND started_at < now() - timeout;
```

- @Scheduled 기반 단순 스케줄러 사용
- 멀티 인스턴스에서도 안전 (멱등 UPDATE)

---

## 7. outbox publisher

```sql
SELECT *
FROM outbox_events
WHERE status = 'NEW'
ORDER BY created_at
FOR UPDATE SKIP LOCKED
LIMIT N;
```

- publish 성공 → `SENT`
- 실패 → 상태 유지 + backoff
- 중복 발행 가능(정상)

---

## 8. RabbitMQ consumer

1. `event_id` 기준 처리 이력 조회
2. 이미 처리됨 → ACK
3. 최초 처리

    - 알림 / 정산 / 후처리
    - 처리 이력 기록
    - ACK
4. 중간 실패

    - ACK 안 함
    - 재전달은 멱등 소비로 흡수

---

## 9. observability

### 로그 키

- `idempotency_key`
- `transferId`
- `event_id`

### 메트릭

- IN_PROGRESS 체류 시간
- FAILED 비율
- outbox backlog 크기

