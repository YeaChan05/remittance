# API

## 개요
- goal: 송금/결제 요청에 사용할 멱등키(Idempotency-Key)를 사전에 발급한다.  
        클라이언트는 이후 실제 송금/결제 API 호출 시 이 키를 사용한다.
- endpoint: `/idempotency-keys`
- Content-Type: `application/json`
- Authorization: `Bearer {access_token}`

## request
- body: x

```http request
POST /idempotency-keys
Content-Type: application/json
Authorization: `Bearer {access_token}`

{
}
````

## response

- status: `200 OK`
- body

    - idempotencyKey: 발급된 멱등키(UUID)
    - expiresAt: 멱등키 유효 만료 시각(ISO-8601)

```json
{
  "idempotencyKey": "3f9c2b1e-8b2a-4c7d-9a4e-6a9d8c2f1e44",
  "expiresAt": "2026-01-08T10:30:00"
}
```

## error

- status: `500 Internal Server Error`
- context: 멱등키 생성 또는 저장 중 서버 내부 오류
