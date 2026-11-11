# 활동 경험치 이벤트와 소급 운영

Community는 점수·등급을 계산하지 않는다. 원본 변경과 같은 트랜잭션으로 `activity_outbox`에 활동의 현재 상태를 기록하고 `USER_ACTIVITY_EVENT`로 전달한다. Auth가 사용자 인증 자격, 지급·회수, 등급을 결정한다. 계약 원본은 `contracts/user-activity.schema.json`, 합성 예시는 `contracts/user-activity.example.json`이다.

## 지급 이벤트

- 일반 글 최초 발행, 일반/의장 댓글·답글, 의장 글, 길드 추억글·모집글, 승인 완료한 길드 등록을 기록한다. 길드 등록의 행위자·작성자는 심사자가 아닌 등록 신청자다.
- 기존 일반 글·댓글·의장 좋아요는 하나의 LIKE 이벤트로 보내며 Auth가 양쪽 수령자를 판단한다. 자기 좋아요는 발행하지 않는다. 좋아요 키는 DB의 재생성되는 좋아요 ID가 아니라 대상+행위자다.
- 수정·임시저장·북마크·조회·길드 신청/거절·모집 종료·탈퇴는 지급 이벤트를 만들지 않는다. 삭제와 좋아요 취소는 inactive 이벤트를 만든다. `post.first_published_at`은 최초 발행 시각을 보존한다. 이미 발행한 글을 임시저장 상태로 바꾸는 것은 수정이므로 XP를 회수하거나 재발행 때 다시 지급하지 않는다. 이후 삭제하면 보존 중인 글·댓글·좋아요 XP를 모두 회수한다. 롤링 배포 중 구버전이 만든 발행 시각 NULL인 공개 글은 수정 전에 기존 생성 시각으로 보정하며 추가 지급하지 않는다.
- 일반 글 삭제는 댓글의 post_id가 끊기기 **전에** 해당 글·댓글·좋아요를 캡처한다. 원댓글 삭제는 남아 있는 답글 경험치를 회수하지 않는다.
- 일반 글/댓글은 루트 글을 잠근 뒤 변경한다. 의장은 기존 의장 글 잠금, 길드는 기존 길드 잠금을 사용한다. 같은 활동의 커밋 순서와 outbox 순번이 일치한다.

## 단계별 활성화

운영 환경을 변경하거나 이관 명령을 실행하려면 배포 승인을 먼저 받아야 한다. 기본 배포는 다음 두 환경 변수가 모두 false이므로 기록/전송을 시작하지 않는다.

| 변수 | 기본값 | 의미 |
| --- | --- | --- |
| `EXPERIENCE_CAPTURE_ENABLED` | false | 원본 변경 트랜잭션에서 활동 outbox 기록 |
| `EXPERIENCE_PUBLISH_ENABLED` | false | 미발행 outbox 전송 |

1. Auth의 추가 스키마·인증 기간 기록·소비자와 Community의 추가 스키마/등급 projection을 먼저 배포한다. Auth의 처리 gate는 닫아 둔다.
2. 모든 Community 인스턴스에 capture를 활성화한다. 구버전 인스턴스와 진행 중인 구버전 요청이 끝난 것을 확인한다. 이 이후의 source 변경은 기록이 유실되어서는 안 되므로 capture를 끄지 않는다.
3. Auth 운영 도구로 T0와 인증 회원 cohort를 고정한다. 반환된 **같은 UTC T0**를 Community freeze 명령에 사용한다. T0 전에 이미 기록한 live 이벤트도 Auth의 동일 cohort 정책에 따른다.
4. 아래 dry-run으로 소급 범위를 확인하고 run을 반복한다. 실시간 원본 변경은 계속 가능하다. sourceRevision=0인 소급 이벤트는 양수 순번의 live 삭제·취소를 덮지 못한다.
5. publisher와 Auth 처리를 활성화하고 backlog를 소진한다. Auth의 이력 합계·집계·등급 대조가 통과한 후 프런트 경험치 화면을 공개한다.

## 소급 명령

배포된 추가 스키마에 접근할 수 있는 기존 DB 환경 변수를 사용한다. 전용 진입점은 datasource/transaction과 소급 서비스만 로드하며 웹 서버·Kafka listener·스케줄러·Flyway를 시작하지 않는다. 작업을 한 번 수행한 후 종료한다. 환경 파일이나 비밀값을 명령행·로그에 출력하지 않는다.

```text
java -Dloader.main=until.the.eternity.dcs.domain.experience.ActivityBackfillCommand -cp <application.jar> org.springframework.boot.loader.launch.PropertiesLauncher <아래 인자>
```

공통 인자:

```text
--experience.backfill.command-enabled=true
```

순서별 추가 인자:

```text
# 1. Auth에서 확정한 T0를 그대로 전달한다. 기존 다른 T0로 재고정할 수 없다.
--experience.backfill.operation=freeze --experience.backfill.cutoff=2026-11-11T09:00:00Z
# 2. 변경 없이 source별 cutoff/현재 cursor/완료/남은 유효 활동 수 확인
--experience.backfill.operation=dry-run
# 3. source별 최대 100개 원본 ID 처리 후 종료. 모든 complete=true가 될 때까지 재실행
--experience.backfill.operation=run --experience.backfill.batch-size=100
```

`activity_backfill_checkpoint`의 각 source에 cutoff ID·T0·last ID가 저장된다. 원본 ID 기준으로 최대 1,000개씩 읽으므로 삭제된 구간도 유한하게 진행한다. 해당 batch의 outbox와 cursor는 한 트랜잭션으로 커밋한다. 실패 시 같은 batch가 재실행되며 활동 키에서 만든 결정적 eventId로 중복 적재를 방지한다. 운영 중 checkpoint를 직접 수정하거나 새 cohort를 만드는 방식으로 재실행하지 않는다.

소급은 현재 남아 있는 유효 활동만 읽는다. 삭제/차단/임시저장 글, 삭제된 부모의 댓글·좋아요, 자기 좋아요는 제외한다. 폐쇄됐지만 삭제되지 않은 모집글은 포함한다. 취소되어 물리 삭제된 과거 좋아요는 복원하지 않는다.

### 과거 시각의 시간대

라이브 이벤트와 outbox/lease 시각은 UTC다. 새 경험치 DATETIME 컬럼은 JDBC `LocalDateTime`으로 읽고 쓰며 JVM·연결 시간대에 따른 `Timestamp` 재변환을 적용하지 않는다. 기존 원본 컬럼은 저장 방식이 다르므로 소급만 아래 변환을 적용한다.

- 일반 글·댓글·좋아요: `experience.backfill.legacy-zone`, 기본 `Asia/Seoul` (기존 운영 JVM `TZ=Asia/Seoul`).
- 길드 등록·추억글·모집글: `experience.backfill.guild-zone`, 기본 `Asia/Seoul` (운영 MySQL `--default-time-zone=+09:00`).
- 의장 관련 원본: 기존 코드가 명시적으로 UTC를 저장하므로 UTC 고정.

다른 시간대로 생성한 로컬 fixture에서는 위 두 옵션을 실제 원본 저장 시간대로 지정한다. DB DATETIME을 임의로 UTC라고 가정해 T0 경계나 활동 시각을 9시간 이동시키지 않는다.

## 실패·복구·확인

- publisher는 MySQL `FOR UPDATE SKIP LOCKED`로 한 행에 120초 lease를 잡고, 브로커 확인 후에만 published_at을 기록한다. 브로커 응답 유실·프로세스 재시작은 같은 eventId 재전송으로 복구한다.
- lease token이 다른 늦은 publisher는 완료/실패 상태를 덮지 못한다. 재시도는 최대 300초 간격이며 미확인 행을 폐기하지 않는다. 수신 실패의 DLT 재처리는 Auth가 소유한다.
- pending 수·가장 오래된 대기 시간·attempts 증가를 관측한다. payload/사용자 식별 정보를 경고 로그에 출력하지 않는다. 중단 시 publisher만 끄고 capture는 유지한다.
- `user_summary.activity_grade`는 인증된 작성자의 표시용 값이다. 기존 `grade`/JWT 권한을 변경하지 않으며 Auth revision이 감소한 스냅샷은 무시한다.

검증: `./gradlew verify --no-daemon`, `./gradlew experienceMysqlTest --no-daemon` (격리된 localhost `TEST_MYSQL_URL`, `TEST_MYSQL_USER`, `TEST_MYSQL_PASSWORD` 필요), 기존 `outfitMysqlTest`, `guildMysqlTest`. PR MySQL workflow가 세 통합 태스크를 실행한다.
