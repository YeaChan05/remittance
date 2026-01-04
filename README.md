# Remittance Service

계좌 등록/삭제, 입금/출금/이체, 거래내역 조회를 제공하는 송금 서비스 과제를 구현하는 프로젝트입니다.

## 문서
- [코드 컨벤션](docs/rule/code_convention.md)
- [모듈 구조](docs/rule/module.md)
- [의존성 규칙](docs/rule/dependencies.md)

## API 요약
- 계좌 등록/삭제
- 입금/출금(일 한도 체크)
- 이체(수수료 1%, 일 한도 체크)
- 거래내역 조회(최신순)

## 실행 방법
- `./gradlew :account:application-api:bootRun`
- `./gradlew :ledger:application-api:bootRun`
- `./gradlew :member:application-api:bootRun`
- `./gradlew :aggregate:bootRun`
- Docker Compose 파일은 각 모듈의 `src/main/resources/docker-compose.yml`에 있습니다.
- aggregate 모듈은 여러 도메인 애플리케이션을 한 곳에 묶어 실행하는 통합 실행용 모듈입니다.
