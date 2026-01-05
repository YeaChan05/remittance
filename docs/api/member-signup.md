# 회원가입

## 개요
- goal: 신규 회원 등록
- endpoint: `POST /members`
- Content-Type: `application/json`

## requset
- body
  - `name`: 회원 이름 (문자열)

```http request
POST /members
Content-Type: application/json

{
  "name": "홍길동"
}
```

## response
- status: `200 OK`
- body
  - `name`: 등록된 회원 이름 (문자열)


```json
{
  "name": "홍길동"
}
```
