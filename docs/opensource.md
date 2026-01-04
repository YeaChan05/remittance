# 외부 라이브러리 및 오픈소스 사용 현황

## 애플리케이션 의존성

- Springdoc OpenAPI UI: `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0` - API 문서/Swagger UI 제공
- Micrometer Tracing(Otel Bridge): `io.micrometer:micrometer-tracing-bridge-otel` - 분산 추적
- JSpecify: `org.jspecify:jspecify` - null 표기 등 타입 주석 지원
- MySQL Connector/J: `com.mysql:mysql-connector-j` - MySQL DB 연결 드라이버

## 테스트/개발 도구

- Testcontainers: `org.testcontainers:testcontainers`, `org.testcontainers:junit-jupiter`, `org.testcontainers:mysql` - 통합 테스트용 컨테이너 기반 DB 환경

## 빌드 플러그인

- Spotless: `com.diffplug.spotless` - 코드 포맷팅
- Kover: `org.jetbrains.kotlinx.kover` - 테스트 커버리지 리포트/검증
- Build Recipe Plugin: `com.linecorp.build-recipe-plugin` - 멀티모듈 공통 Gradle 설정

## CI 오픈소스 액션

- sdkman/sdkman-action@v1 - SDKMAN 기반 Java 설치
- codecov/codecov-action@v5.5.2 - 커버리지 업로드
