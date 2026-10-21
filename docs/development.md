# 개발과 검증

## 검증

Java 21을 설치하고 JAVA_HOME을 지정한다. Gradle은 저장소 wrapper를 사용한다.

```powershell
.\gradlew.bat verify --no-daemon
```

Linux/macOS/CI: `./gradlew verify --no-daemon`. check(테스트와 설정된 정적 검사) 및 bootJar를 실행한다. community/batch에는 spotlessCheck가 포함된다. 자동 포맷과 src 하위 REST Docs 재생성은 실행하지 않는다. 테스트 보고서는 build/reports/tests/test/index.html에 있다. 테스트 실패와 Java/의존성 다운로드 환경 오류를 구분해 기록한다.

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

PR 필수 검사 이름은 verify와 secret-scan이다. 원격 브랜치 보호에서 두 검사와 사람 리뷰를 필수로 지정해야 강제된다. yaml 파일만 추가해도 보호가 활성화되는 것은 아니다. 이미지 게시/운영 배포는 workflow_dispatch로 분리했고 운영은 main/production, 개발은 dev/development로 제한한다. GitHub Environment에 승인자를 별도로 설정해야 실제 사람 승인 게이트가 생긴다. push/PR 생성만으로 배포하지 않는다.

각 서비스의 Compose는 개별 개발 환경이다. 전체 MSA가 자동 연결된다는 뜻은 아니다. Kafka는 bootstrap 주소뿐 아니라 브로커 advertised.listeners가 각 애플리케이션 컨테이너에서 접근 가능한 주소여야 한다. 호스트용 localhost 광고 주소를 컨테이너에 그대로 사용하지 않는다. 통합 환경에서는 공통 네트워크의 내부 리스너를 사용하거나 개발 호스트 주소로 광고하도록 별도 구성한다.

## 공동 로컬 인프라 연결

공동 루트 가이드로 devnogi-infra-prod의 mysql/kafka를 먼저 시작하면 memonogi-local 네트워크가 만들어진다. 기존 Compose 뒤에 -f docker-compose-shared.yml을 추가하면 애플리케이션이 공통 Kafka 내부 리스너(devnogi-local-kafka:29092)를 사용한다. auth는 공통 MySQL에도 연결한다. community/batch의 자체 DB 등은 유지되며 community의 별도 kafka는 선택하지 않는다. 명령: `docker compose --env-file .env.local -f docker-compose-local.yaml -f docker-compose-shared.yml up --build backend`.
