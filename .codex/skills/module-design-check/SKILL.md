---
name: module-design-check
description: 리미턴스 모듈 설계만 점검한다(Driving/Core/Driven/Assembly 경계, 모듈 레이아웃, Gradle 의존 방향). 모듈 구조 변경, 신규 모듈 추가, 의존성 wiring 리뷰에 사용한다.
---

# 모듈 설계 점검 (집중형)

## Language

모든 응답은 한국어로 작성합니다.

이 스킬은 모듈 설계 적합성(구조 + 의존 방향)만 점검한다. API 계약, 비즈니스 로직, 구현 상세는 다루지 않는다.

## 빠른 절차

1) 범위 식별
- 변경 맥락과 `settings.gradle.kts`를 확인한다.
- 도메인이 지정되면 해당 도메인 모듈 그래프만 점검한다.

2) 모듈 구조 점검(트리 수준)
- 도메인 기대 트리:
  `{domain}/model`, `{domain}/infrastructure`, `{domain}/service`, `{domain}/exception`,
  `{domain}/api` + 선택 `{domain}/api-internal`, `{domain}/repository-jpa`,
  `{domain}/schema`, `{domain}/mq-rabbitmq`.
- `aggregate`는 조립 전용이며 비즈니스 로직 금지.
- 허용 예시: `transfer/service`, `transfer/repository-jpa`, `transfer/schema`.
- 금지 예시: `transfer/web`, `transfer/jpa`, `transfer/infra-impl` 같은 비표준 모듈.

3) 의존 방향 점검(Gradle/모듈 wiring)
- 허용 방향: Driving -> Core -> Driven만.
- Core는 어댑터/구현 기술에 의존하면 안 됨.
- 도메인 간 직접 의존은 `model -> model`만 허용.
- `api`는 `service`와 `exception`만 의존.
- `repository-{type}`는 `{domain}:infrastructure`와 `common:repository-{type}`만 의존.
- `aggregate`는 `common:security` + 각 도메인 adapter(api/repository/schema/mq)만 의존.
- `api`는 계약 노출만, `implementation`은 내부 구현에만 사용.
- 허용 예시:
  - `transfer:api -> transfer:service`
  - `transfer:service -> transfer:infrastructure`
  - `transfer:repository-jpa -> transfer:infrastructure`
- 금지 예시:
  - `transfer:service -> transfer:repository-jpa`
  - `account:api -> account:repository-jpa`
  - `member:service -> auth:api` (도메인 간 직접 의존)

4) 결과 보고
- 심각도 순으로 이슈를 파일 경로와 함께 정리한다.
- 문제가 없으면 “문제 없음”과 가정/누락을 적는다.

5) 의존 변화 비교(해당 시)
- Gradle 수정이 있으면 모듈 간 연결 추가/삭제를 요약한다.
- 새로 추가된 cross-domain 의존을 강조한다.

## 출력 템플릿

다음 구조로 결과를 작성한다:

- 모듈 그래프 요약:
    - <domain>: <Driving/Core/Driven 모듈 구성 및 누락/초과>
- 발견 사항:
    - [severity] path:line - 이슈 요약 및 규칙 근거
- 의존 변화:
    - + <from> -> <to> (이유)
    - - <from> -> <to>
- 가정:
    - ...
- 누락/후속 확인:
    - ...

## 비고

- `rg`로 파일을 찾고 Gradle 파일을 우선 확인한다.
- 코드는 수정하지 않고 설계 점검만 수행한다.
