# 인증 필터

## 목표

- 도메인별 API에서 인증 정책을 정의하고, 인증 검증 로직은 공통화한다.
- 도메인 간 의존 규칙을 유지하면서 MSA 확장성을 확보한다.

## 핵심 원칙

- 토큰 발급은 `auth:service`의 책임이다.
- 회원 자격 검증은 `member:service`가 담당하고 `member:api-internal`로 제공한다.
- 토큰 검증/파싱은 공통 모듈로 이동한다.
- 공통 인증 설정은 `common:security`에 둔다.
- 각 도메인 `api` 모듈에서 `AuthorizeHttpRequestsCustomizer` 빈으로 경로별 인증 정책을 지정한다.

## 역할 분리

- `auth:service`
    - 로그인 유스케이스에서 토큰 발급
    - `TokenGenerator` 사용
    - `MemberInternalApi`로 자격 검증 요청
- `member:service`
    - 자격 검증 유스케이스(`MemberAuthQueryUseCase`) 제공
- `member:api-internal`
    - 내부 계약(`LoginVerifyRequest`/`LoginVerifyResponse`) 제공
    - `MemberInternalApi` 인바운드 어댑터 제공
- `common` (예: `common:security`)
    - `TokenVerifier`/`TokenParser` 제공
    - 공통 인증 필터(`OncePerRequestFilter`) 제공
    - 공통 보안 설정(예: 필터 빈, 기본 인증/인가 핸들러)
- 각 `{domain}:api`
    - `AuthorizeHttpRequestsCustomizer`로 인증/인가 경로 정의

### 공통 모듈 구성 예시

- `CommonSecurityAutoConfiguration`
    - `JwtAuthenticationFilter`, `AuthenticationEntryPoint`, `AccessDeniedHandler` 빈 제공
    - `TokenParser`/`TokenVerifier` 빈 제공
    - `SecurityFilterChain` 기본 구성 제공
- `AuthorizeHttpRequestsCustomizer`
    - 도메인별 `authorizeHttpRequests` 정책을 분리하기 위한 인터페이스
    - 기본 구현은 모든 요청 인증

### 도메인별 구성 예시

```java
@Configuration
public class DomainSecurityConfiguration {
  @Bean(name = "authorizeHttpRequestsCustomizer")
  AuthorizeHttpRequestsCustomizer authorizeHttpRequestsCustomizer() {
    return registry -> registry
        .requestMatchers(HttpMethod.POST, "/path").permitAll()
        .anyRequest().authenticated();
  }
}
```

## 설정 공유

- 토큰 검증에 필요한 `salt`와 만료 시간은 각 서비스 `application.yml`에서 동일한 키로 제공한다.
- 예시

```yaml
auth:
  token:
    salt: remittance-token-salt
    access-expires-in: ${AUTH_TOKEN_ACCESS_EXPIRES_IN:3600}
    refresh-expires-in: ${AUTH_TOKEN_REFRESH_EXPIRES_IN:604800}
```

## 도메인 의존성 고려

- `account:api`가 `member:service`에 의존하면 도메인 간 직접 의존이 발생한다.
- 인증 필터가 필요한 토큰 검증 기능은 공통 모듈로 이동해야 한다.

## 권장 흐름

1. `member:service`에서 자격 검증 유스케이스 제공
2. `member:api-internal`에서 내부 인증 계약/어댑터 제공
3. `auth:service`에서 로그인 처리 및 토큰 발급
4. `common`에 인증 필터 및 토큰 검증/파싱 구성
5. 각 도메인 `api`에서 `AuthorizeHttpRequestsCustomizer`로 경로 정책 정의

## 관리 엔드포인트 처리

- `common:security`가 기본 `SecurityFilterChain`을 제공할 경우, Actuator 보안 체인과 충돌할 수 있다.
- 현재 구성에서는 각 애플리케이션에서 다음 설정으로 관리 체인을 제외한다.

```yaml
spring:
  autoconfigure:
    exclude: org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration
```

## Aggregate 정책 합집합

- `aggregate`는 여러 도메인을 조합한 API를 제공하므로, 각 도메인의 `permitAll`/`authenticated` 규칙 합집합으로 구성한다.
- 예시

```java
@Configuration
public class AggregateSecurityConfiguration {
  @Bean(name = "authorizeHttpRequestsCustomizer")
  AuthorizeHttpRequestsCustomizer authorizeHttpRequestsCustomizer() {
    return registry -> registry
        .requestMatchers(HttpMethod.POST, "/login", "/members").permitAll()
        .anyRequest().authenticated();
  }
}
```

## 결론

- 발급은 `auth` 책임
- 자격 검증은 `member` 책임
- 토큰 검증은 `common` 책임
- 공통 설정은 `common:security` 책임
- 정책은 각 도메인 `api` 책임
