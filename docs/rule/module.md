### 모듈 구조 및 규칙

---

## 1. 최상위 원칙

* 모든 도메인은 Core 모듈 + 구현 모듈 조합으로 구성한다.
* 의존성은 항상 Core 방향(안쪽)으로만 흐른다.
* 기술(JPA, Web, MQ 등)은 구현 모듈에만 존재한다.
* 다른 애플리케이션과의 연결은 in-port(in-port-internal) 계약으로만 한다.

---

## 2. 도메인 모듈 트리 규칙

```text
{domain}
 ├── model
 ├── infrastructure
 ├── service
 ├── exception
 ├── api
 ├── repository-{type}
 └── application-{type}
```

* `repository-{type}`: `jpa|jdbc|mongo` 등 저장 방식에 따라 분기
* `application-{type}`: 실제 실행(조립) 모듈, `api|batch` 등

---

## 3. Core 모듈 규칙

### 3.1 model

* 도메인 핵심 개념(상태/규칙)을 담는다.
* 프레임워크/외부기술 의존 금지.
* 원칙적으로 다른 하위 모듈 의존 금지(필요 시 다른 도메인 model 의존은 허용 가능).

의존

* (가능하면 없음)

노출(Gradle)

* 외부에 노출해도 되는 도메인 타입만 포함

---

### 3.2 infrastructure (out-port)

* 외부세계와의 연결 "규격(포트)"만 정의한다.
* 구현체 금지.
* repository, external-client, event-publisher 같은 interface만 둔다.

의존

* `model`

노출(Gradle)

* `api(project(":{domain}:model"))` 형태로 계약은 노출, 구현은 숨김

---

### 3.3 service (use-case)

* 유스케이스 단위의 비즈니스 로직을 담는다.
* 트랜잭션/오케스트레이션은 여기서 끝낸다.
* 외부 의존은 반드시 `infrastructure`에 정의된 포트를 통해서만 한다.

의존

* `model`
* `infrastructure`
* `exception`

금지

* `repository-*`, `api` 등 구현체 직접 의존 x

---

### 3.4 exception

* 모든 비즈니스 예외는 반드시 `BusinessException`을 상속해야 한다.
* 기술 예외를 그대로 던지지 않는다(필요 시 변환).

의존

* `common:exception` (또는 동일 역할 공통 모듈)

금지

* 스프링 웹/DB 예외 타입 직접 의존 x

---

## 4. 구현(Adapter) 모듈 규칙

### 4.1 api (Inbound Adapter)

* Controller + request/response DTO만 포함한다.
* 비즈니스 로직 금지.
* 호출 대상은 `service` 또는 `in-port-internal`로 제한한다.

의존

* `common:api`
* `service`
* `exception`

금지

* `repository-*` 직접 의존 x
* `model`을 그대로 응답으로 노출 x(필요 시 api DTO로 변환)

---

### 4.2 repository-{type} (Outbound Adapter)

* `infrastructure`에 정의된 persistence 포트를 구현한다.
* JPA Entity / Spring Data Repository / Mapper는 여기만 존재한다.
* Domain ↔ Entity 변환 책임은 여기(Impl)에 둔다.

의존

* `common:repository-{type}`
* `infrastructure`

금지

* `api` 의존 x
* `service` 의존 x (service가 포트를 호출해야 함)

---


### 4.3 application-{type} (조립/실행, 선택)

* "실행 가능한 애플리케이션" 모듈.
* 각 adapter들의 Bean을 조립하기 위해 auto-configuration 의존을 둔다.
* 통합 테스트 유틸리티를 이 계층에 둔다.

의존(일반)

* `{domain}:repository-{type}`
* `{domain}:api` 또는 `{domain}:service`
* `common:application-{type}`

금지

* 도메인 규칙/로직 구현 x
* auto-configuration을 통한 bean 등록을 위해 의존만

---

## 5. 애플리케이션 간 연동 규칙

* 직접 참조 최소

    * 가능하면 다른 도메인의 `model/service/repository`를 의존 x
* 허용

    * `api-internal-client` ↔ 상대 앱 `in-port-internal`
* 연결 방식

    * 런타임(HTTP/MQ) 연결은 간접(Indirect) 관계로만 표현

---

## 6. Gradle 노출 규칙(api vs implementation)

* `api` 사용 대상

    * 계약(인터페이스, 타입)을 외부 모듈이 컴파일 타임에 알아야 할 때
    * 예: `infrastructure -> model`을 `api(...)`로 노출
* `implementation` 사용 대상

    * 내부 구현 세부사항(교체 가능해야 하는 것)
    * 예: `service -> infrastructure`, `api -> service`, `repository-* -> infrastructure`

---

원하면, 네가 쓰는 실제 도메인 하나(예: `member`나 `account`)를 잡고
"각 모듈의 build.gradle.kts 의존성 블록"을 이 규칙대로 딱 맞게 예시로 만들어줄게.
