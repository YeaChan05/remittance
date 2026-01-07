# 계좌 생성

## 개요
- goal: 계좌 등록
- endpoint: `POST /accounts`
- Content-Type: `application/json`
- Authorization: `Bearer {accessToken}`

## request
- body
  - `bankCode`: 은행 코드 (문자열)
  - `accountNumber`: 계좌 번호 (문자열)
  - `accountName`: 계좌 별칭 (문자열)

```http request
POST /accounts
Content-Type: application/json
Authorization: Bearer {accessToken}

{
  "bankCode": "090",
  "accountNumber": "123-456-789",
  "accountName": "생활비 계좌"
}
```

## response
- status: `200 OK`
- body
  - `accountId`: 계좌 식별자 (숫자)
  - `accountName`: 계좌 별칭 (문자열)

```json
{
  "accountId": 101,
  "accountName": "생활비 계좌"
}
```

## error
- status: `400 Bad Request`
- context
  - `bankCode` is invalid
  - `accountNumber` is invalid
  - `accountName` is invalid

- status: `401 Unauthorized`
- context
  - authentication required

- status: `400 Bad Request`
- context
  - duplicate account
