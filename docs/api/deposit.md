# 입금 API

## 개요

- goal: 특정 계좌에 입금한다.
- endpoint: `POST /deposits/{idempotencyKey}`
- Content-Type: `application/json`

## request

- path
    - `idempotencyKey`: 멱등키
- body
    - `accountId`: 입금 계좌 ID
    - `amount`: 입금 금액

```http request
POST /deposits/{idempotencyKey}
Content-Type: application/json

{
  "accountId": 1,
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
  "transferId": 789,
  "errorCode": null
}
```

## error

- status: `400 BAD_REQUEST`
- context
    - `INVALID_REQUEST`
    - `ACCOUNT_NOT_FOUND`
