# 계좌 이체 API

## 개요

- goal: 출금 계좌에서 다른 계좌로 이체한다.
- endpoint: `POST /transfers/{idempotencyKey}`
- Content-Type: `application/json`
- 수수료: 이체 금액의 1%를 출금 계좌에서 추가 차감한다.

## request

- path
  - `idempotencyKey`: 멱등키
- body
  - `fromAccountId`: 출금 계좌 ID
  - `toAccountId`: 입금 계좌 ID
  - `amount`: 이체 금액

```http request
POST /transfers/{idempotencyKey}
Content-Type: application/json

{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 10000
}
```

## response

- status: `200 OK`
- body
  - `status`: `SUCCEEDED | FAILED | IN_PROGRESS`
  - `transferId`: 거래 ID
  - `errorCode`: 실패 코드

```json
{
  "status": "SUCCEEDED",
  "transferId": 456,
  "errorCode": null
}
```

## error

- status: `400 BAD_REQUEST`
- context
  - `INVALID_REQUEST`
  - `INSUFFICIENT_BALANCE`
  - `DAILY_LIMIT_EXCEEDED`
