# 계좌 삭제

## 개요
- goal: 계좌 삭제
- endpoint: `DELETE /accounts/{accountId}`
- Content-Type: `application/json`
- Authorization: `Bearer {accessToken}`

## request
- path
  - `accountId`: 계좌 식별자 (숫자)

```http request
DELETE /accounts/101
Content-Type: application/json
Authorization: Bearer {accessToken}

```

## response
- status: `200 OK`
- body
  - `accountId`: 삭제된 계좌 식별자 (숫자)

```json
{
  "accountId": 101
}
```

## error
- status: `400 Bad Request`
- context
  - `accountId` is invalid
  - `memberId` is invalid

- status: `401 Unauthorized`
- context
  - authentication required
  - account owner mismatch

- status: `404 Not Found`
- context
  - account not found
