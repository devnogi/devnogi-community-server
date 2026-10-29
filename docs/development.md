# 개발과 검증

## 검증

Java 21을 설치하고 JAVA_HOME을 지정한다. Gradle은 저장소 wrapper를 사용한다.

```powershell
.\gradlew.bat verify --no-daemon
```

Linux/macOS/CI: `./gradlew verify --no-daemon`. check(테스트와 설정된 정적 검사) 및 bootJar를 실행한다. community/batch에는 spotlessCheck가 포함된다. 자동 포맷과 src 하위 REST Docs 재생성은 실행하지 않는다. 테스트 보고서는 build/reports/tests/test/index.html에 있다. 테스트 실패와 Java/의존성 다운로드 환경 오류를 구분해 기록한다.

### Redis 알림 실검증

일반 `verify`는 외부 Redis 없이 실행한다. 실제 stream/group·pending·ACK와 알림 DB 트랜잭션은 별도 `redisIntegrationTest`로 확인한다. 기존 JUnit/H2와 실제 Redis를 사용하며, PR의 필수 `verify` job이 표준 검증 후 Redis 7.4.9에서 이 task를 실행한다. 어느 단계든 실패하면 필수 검사가 실패한다.

로컬에서는 운영 데이터가 없는 별도 Redis 7을 준비하고 접속 위치를 명시한다. 예를 들어 테스트 Redis를 loopback 포트 16389에 띄웠다면 다음과 같다.

```powershell
$env:TEST_REDIS_HOST = '127.0.0.1'
$env:TEST_REDIS_PORT = '16389'
.\gradlew.bat redisIntegrationTest --no-daemon
```

Linux/macOS: `TEST_REDIS_HOST=127.0.0.1 TEST_REDIS_PORT=16389 ./gradlew redisIntegrationTest --no-daemon`. 변수가 없거나 Redis에 연결할 수 없으면 task가 실패한다. skip으로 통과시키지 않으며 요청할 때마다 다시 실행한다. 보고서는 `build/reports/tests/redisIntegrationTest/index.html`, Redis 버전과 record ID·pending 관찰은 `build/test-results/redisIntegrationTest/`에 남는다.

각 테스트는 고유 stream/group과 notification ID를 생성하고 자신이 만든 정확한 Redis 키만 정리한다. `FLUSHALL`이나 실제 `notif-stream` 정리는 하지 않는다. H2의 독립 테스트 DB에서 실제 `NoticeService` 트랜잭션을 실행하며, 수신자 저장 제약을 시험 중에만 추가해 부분 저장 롤백·자기 claim 해제·pending 보존·명시적 같은 레코드 재전달을 확인한다. MySQL/Flyway와 auth→Kafka→community 전체 경로는 별도 격리 Docker 통합으로 확인해야 한다.

알림 소비는 정확한 과거 `{mk: '1'}` 잔재만 ACK한다. 그 외 malformed 레코드와 확인된 DB 실패는 ACK하지 않아 pending에 남긴다. 이는 자동 재시도가 아니며 자동 reclaim/replay·DLQ·exactly-once 보장을 포함하지 않는다. 기존 중복 키만 보고 과거 알림 저장 성공을 단정하거나 운영 키를 일괄 삭제하지 않는다.

## 로컬 Docker

Compose에 MySQL(호스트 3319), Redis(6379), MinIO(9000/9001), Kafka(9092)가 있다. 다른 로컬 컨테이너와 포트 충돌을 확인한다. MinIO bucket은 개발용으로 별도 생성한다.

서비스 폴더에서 아래 명령을 사용한다. 기존 .env.local이 있으면 덮어쓰지 말고 필요한 키를 비교한다.

```powershell
Copy-Item .env.example .env.local
docker compose --env-file .env.local -f docker-compose-local.yaml config --quiet
docker compose --env-file .env.local -f docker-compose-local.yaml up --build backend kafka
```

기본 포트: 8093. Compose의 env_file과 interpolation은 다르므로 --env-file을 생략하지 않는다. Linux에서는 host.docker.internal host-gateway 설정이 필요할 수 있다. 필요한 서비스만 지정해 Docker socket을 마운트하는 autoheal은 실행하지 않는다. config 통과는 기동·DB migration·외부 연동 성공의 증거가 아니다. 중단은 같은 파일과 --env-file로 stop을 실행한다. down -v는 데이터를 삭제하므로 별도 판단한다.

## CI와 배포

PR과 수동 진단은 공통 `spring-ci`의 `community-redis` 프로필을 사용한다. 이 프로필은 표준 verify, Redis 7.4.9 통합 테스트, secret scan, Docker 빌드와 운영 Compose 렌더링을 실행한다. 새 필수 검사 context는 실제 PR에서 확인하고 기존 브랜치 보호를 유지한 채 전환한다.

`main` push는 같은 검증을 통과한 뒤 이미지를 게시하고 `production` 환경에 digest로 배포한다. 복구용 수동 실행도 `main`에서만 가능하다. PR 생성만으로는 배포하지 않으며, 배포 경로 활성화 후 승인된 PR의 main 병합이 운영 배포 승인이다. 수동 복구 실행은 별도 사람 승인이 필요하다. 개발 배포 workflow는 별도로 유지한다.

운영 Compose의 `backend` 이미지는 필수 `IMAGE_REF`로 지정한다. 공통 배포는 컨테이너 실행 상태와 이미지 ID를 확인하고, Docker healthcheck가 설정된 경우 그 상태도 확인한다. 현재 운영 Compose에는 Docker healthcheck가 없으므로 HTTP 검사가 필수다. `PROD_HEALTH_URL`은 서버의 loopback에서 접근 가능한 이 서비스의 `/actuator/health`로 설정하고, 200 응답을 최대 300초 기다린다. 이 검사는 DB와 Redis 상태가 비활성화된 기존 actuator 설정을 따른다. 실패 시 같은 실행이 소유한 이전 구성과 이미지를 복원하고 상태를 다시 검사한다.

각 서비스의 Compose는 개별 개발 환경이다. 전체 MSA가 자동 연결된다는 뜻은 아니다. Kafka는 bootstrap 주소뿐 아니라 브로커 advertised.listeners가 각 애플리케이션 컨테이너에서 접근 가능한 주소여야 한다. 호스트용 localhost 광고 주소를 컨테이너에 그대로 사용하지 않는다. 통합 환경에서는 공통 네트워크의 내부 리스너를 사용하거나 개발 호스트 주소로 광고하도록 별도 구성한다.

## 공동 로컬 인프라 연결

공동 루트 가이드로 devnogi-infra-prod의 mysql/kafka를 먼저 시작하면 memonogi-local 네트워크가 만들어진다. 기존 Compose 뒤에 -f docker-compose-shared.yml을 추가하면 애플리케이션이 공통 Kafka 내부 리스너(devnogi-local-kafka:29092)를 사용한다. auth는 공통 MySQL에도 연결한다. community/batch의 자체 DB 등은 유지되며 community의 별도 kafka는 선택하지 않는다. 명령: `docker compose --env-file .env.local -f docker-compose-local.yaml -f docker-compose-shared.yml up --build backend`.
