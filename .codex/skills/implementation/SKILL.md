---
name: implementation
description: Implement or modify code in the remittance repo by learning and following existing codebase conventions, module boundaries, and naming/structure patterns. Use when adding or changing Java/Gradle code in any module.
---

# Implementation (Codebase-Driven)

## Language

모든 응답은 한국어로 작성합니다.

## 핵심 원칙

- 문서가 아니라 코드베이스의 실제 구현 패턴을 기준으로 작업한다.
- 같은 역할의 기존 파일을 먼저 찾고, 그 구조와 스타일을 그대로 복제한다.
- 규칙은 외부 링크가 아니라 이 스킬 본문과 코드에서만 추론한다.

## 작업 시작 루틴

1) 유사 구현 탐색
- 수정하려는 기능과 가장 가까운 구현을 먼저 찾는다.
- 모듈/패키지/클래스 구조를 그대로 따른다.

2) 패턴 추출
- 클래스/레코드 형태, 메서드 시그니처, 로깅 키, 예외 처리, DTO/Props/Identifier 설계를 추출한다.

3) 동일 패턴으로 구현
- 새 코드는 기존 패턴을 1:1로 복제해 작성한다.
- 구조가 다른 코드를 만들지 않는다.

4) 검증
- 컴파일/테스트가 깨지지 않게 최소 변경으로 반영한다.

---

## 코드베이스에서 관찰된 구현 패턴

### API 계층 (api)
- Controller는 `record`로 선언하고 생성자 주입을 사용한다.
- `@RestController`, `@RequestMapping`를 사용한다.
- Controller는 `{Domain}Api` 인터페이스를 구현한다.
- 요청 DTO는 `dto` 패키지의 `record`를 사용한다.
- `@LoginUserId` 파라미터를 사용한다.
- 요청 파라미터는 `@RequestBody`, `@PathVariable`, `@RequestParam`과 `@Valid`를 사용한다.
- 비즈니스 로직은 Controller에 두지 않는다.

### 서비스 계층 (service)
- UseCase 인터페이스 + 구현 `record`를 같은 파일에 둔다.
- 구현체는 `@Slf4j`를 사용하고, 의미 있는 로그 키로 `info/warn/error`를 남긴다.
- 트랜잭션 경계는 서비스에서 설정한다.
- 실패는 도메인 예외로 변환한다.

### 모델/Props/Identifier 패턴
- 모델은 `interface`로 정의하고, Props/Identifier 인터페이스와 조합한다.
- 내부 커맨드 객체는 `record`로 만들고 `Props` 인터페이스를 구현한다.
- 작은 커맨드/식별자 레코드는 private record로 중첩 선언하는 패턴을 사용한다.

### Repository 구현 (repository-jpa)
- 포트 인터페이스를 구현하는 `*RepositoryImpl`을 둔다.
- JPA Repository는 별도 인터페이스로 분리한다.
- Entity 생성은 `Entity.create(props)` 패턴을 사용한다.
- 조회 후 매핑은 `map(entity -> entity)` 형태로 최소 변환을 유지한다.

### 예외 처리
- 예외는 도메인 예외 계층을 사용한다.
- 실패 코드는 enum으로 관리하고, 예외에 포함한다.

### 로깅 스타일
- 로그 키는 `도메인.행위.상태` 형태를 사용한다.
- 주요 파라미터는 `{}` 플레이스홀더로 기록한다.
- 실패 시 `warn`, 예기치 못한 예외는 `error`로 남긴다.

---

## 구현 시 따라야 할 코드 패턴 예시 (찾아볼 파일)

- Controller 패턴: `account/api`의 Controller, `transfer/api`의 Controller
- UseCase + Service record 패턴: `account/service`, `transfer/service`
- Idempotency/Outbox 패턴: `transfer/service` + `transfer/repository-jpa`
- Repository 구현 패턴: `transfer/repository-jpa`의 `*RepositoryImpl`
- 예외 계층: `*/exception` 모듈
- 공통 예외 처리: `common/api`의 `GlobalExceptionHandler`

---

## 반드시 지켜야 하는 스타일

- 들여쓰기 4칸 스페이스.
- Java 코드는 Google Java Format 기준으로 작성.
- 클래스/패키지 네이밍은 기존 패턴을 그대로 따른다.
- 기술 이름을 클래스명에 넣지 않는다.

---

## Output template

- Findings / plan of changes:
    - <module>: <change summary>
- Tests:
    - <unit/integration tests to add or run>
- Risks / assumptions:
    - <edge case or constraint>

## Notes

- Gradle: `./gradlew test`, `./gradlew integrationTest`, `./gradlew check`.
- Format: `./gradlew spotlessApply`.
- 검색은 `rg` 우선.
