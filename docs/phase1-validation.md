# 1차 설정 검증 기록

2026-10-21, Windows PowerShell, Amazon Corretto 21.0.12, 저장소 Gradle wrapper. 로컬 브랜치: codex/ai-workspace-phase1. commit/push/배포는 수행하지 않았다.

| 검사 | 결과 |
| --- | --- |
| .\gradlew.bat verify --no-daemon | 통과 (Spotless, JaCoCo 포함), 최종 재검증 15s |
| JUnit XML 합계 | 131개, 실패 0, 건너뜀 0 |
| node scripts/secret-scan.mjs | finding 0 (현재 트리의 기본 검사, 전체 이력 검사 아님) |
| Docker Compose config --no-env-resolution --quiet | 합성 .env.example 사용, 구문 검사 통과 |
| git diff --check | 통과 |

Docker 데몬 기동·이미지 빌드·전체 MSA 연동·운영 배포·실제 PR/원격 필수 검사 차단은 이 기록의 검증 범위 밖이다. --no-env-resolution은 서비스 env_file 로드를 생략하므로 실제 실행 전 .env.local 준비와 config 재검증이 필요하다.

독립 리뷰 P2 수정 후 test bootJar는 36s에 통과했고, 전체 verify 재검증도 통과했다. 제공자의 미인증 가입 fixture(serverName=null)가 역직렬화되지만 현재 @NotBlank 제약에 걸리는 기존 불일치를 별도 회귀 테스트로 기록했다. 이 테스트는 소비·DB 저장 성공을 주장하지 않는다. root .gitattributes를 루트 소유 경로로 제한한 뒤 기존 서비스 줄바꿈을 보존한 상태에서 Spotless가 통과했다.
