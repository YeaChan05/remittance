## Domain

---

### Ledger

- 무엇이 일어났는가
- 계좌 간 송금이라는 사건(비즈니스 사실)
- A → B로 얼마를 옮겼는지에 대한 의미 단위
- 도메인 핵심 개념 (core)
- 삭제/TTL 대상 아님

---

### Transfer

- 돈이 어떻게 변했는가
- 계좌별 금액 증감 기록(원장)
- 잔액의 근거 데이터
- 회계 관점의 진실(source of truth)
- Transfer 하나가 여러 Ledger 기록을 만들 수 있음
- 도메인 핵심 개념 (core)

---

### Idempotency

- 이 요청을 처리해도 되는가
- 같은 요청을 한 번만 실행하기 위한 제어 장치
- 요청/유스케이스 단위 멱등 보장
- 비즈니스 개념 아님
- 여러 도메인에서 공통 사용
- TTL/정리 대상 (integration 성격)

---

### Outbox

- 이 사실을 외부에 알렸는가
- 도메인에서 발생한 사실을 유실 없이 전달하기 위한 기록
- DB 트랜잭션과 메시지 발행의 간극을 메움
- 중복 발행 허용, 유실 불가
- 비즈니스 개념 아님
- 전달 완료 후 정리 가능 (integration 성격)

---


### 한 줄 요약

- Transfer: 무슨 **송금**이 있었는지
- Ledger: **돈**이 어떻게 변했는지
- Idempotency: 이 요청을 처리해도 되는지
- Outbox: 그 사실을 밖에 제대로 알렸는지

흐름 요약
```text
요청
tx1
 → Idempotency (중복 차단)
 → Transfer (사건 생성/식별)
 → Outbox (이벤트 기록)
tx1 commit
tx2
 → Ledger (금액 변화 기록)
tx2 commit

```
