# 계좌 이체 알림 흐름

계좌 이체 알림은 outbox + RabbitMQ + SSE를 사용해
송금 성공 이후 수신자에게 실시간 알림을 제공한다.
알림 실패는 송금 성공에 영향을 주지 않는다.

---

## 0. 데이터 모델 전제

- 도메인 데이터는 `core`
- 이벤트/처리 이력은 `integration`
- SSE 연결 상태는 메모리 관리

### `integration.outbox_events`

- 송금 완료 시 이미 생성됨
- `event_type = TRANSFER_COMPLETED`
- payload에 수신자 식별 정보 포함

```json
{
  "transferId": "...",
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 10000,
  "completedAt": "..."
}
```

---

### `integration.processed_events` (컨슈머 멱등)

- `event_id` UNIQUE (BIGINT)
- `processed_at`

---

## 1. 송금 완료(outbox 적재)

- 송금 TX 커밋 시 outbox에 `TRANSFER_COMPLETED` 이벤트 저장
- 알림과 직접적인 연계 없음
- 이 시점까지가 송금의 책임 범위

---

## 2. outbox publisher (transfer.infrastructure)

1. `status = NEW` 이벤트 조회
2. RabbitMQ로 publish
3. 성공 시 `status = SENT`
4. 실패 시 재시도(backoff)

> 중복 publish 가능 (정상 케이스)

---

## 3. RabbitMQ consumer (account.mq-rabbitmq)

1. `TRANSFER_COMPLETED` 이벤트 수신
2. `event_id` 기준 처리 이력 조회

### 이미 처리된 경우

- ACK 후 종료

### 최초 처리인 경우

- 알림 처리 단계로 진행

---

## 4. 알림 처리(account.infrastructure)

1. payload에서 `toAccountId` 추출
2. 계좌 -> 사용자(member) 식별
3. 알림 메시지 구성

```json
{
  "type": "TRANSFER_RECEIVED",
  "transferId": "...",
  "amount": 10000,
  "fromAccountId": 1,
  "occurredAt": "..."
}
```

4. `NotificationPushPort` 호출

---

## 5. SSE 전송(account.api)

### 5-1. SSE 연결

- `GET /sse`
- 로그인 사용자 기준으로 연결 등록
- `Map<memberId, SseEmitter>` 형태로 관리

---

### 5-2. 알림 push

- 해당 사용자 SSE 세션 존재 시

    - 즉시 전송
- 세션 없음

    - 무시 (또는 저장형 알림으로 확장 가능)

---

### 5-3. 전송 실패

- 연결 종료 시 세션 정리
- 재시도 없음 (실시간 알림 특성상 허용)

---

## 6. 컨슈머 처리 완료

1. `processed_events`에 `event_id` 기록
2. ACK
3. 동일 이벤트 재수신 시 멱등 흡수

---

## 7. 장애/엣지 케이스

- RabbitMQ 장애 -> outbox 적체 후 복구
- consumer 크래시 -> 재전달 + 멱등 처리
- SSE 미연결 -> 알림 유실 허용
- 중복 이벤트 -> `processed_events`로 차단

---

## 8. observability

### 로그 키

- `transferId`
- `event_id`
- `memberId`

### 메트릭

- 알림 전송 성공/실패 수
- 처리 지연 시간
- SSE 연결 수

---

## 핵심 특성

- 송금 성공과 알림은 강하게 분리
- 알림은 최소 한 번 시도
- 중복/지연/유실은 시스템적으로 허용
- 사용자 UX 개선용 부가 기능

---
