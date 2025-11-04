# [아키텍처] 최소 1회 전송(At-least-once)을 보장하는 알림 시스템

## 1. 시스템 목표
- "하루에 한 번"과 같이 정해진 알림을 **누락 없이** 전송한다. (At-least-once Delivery)
- API 서버 장애, DB 장애, 애플리케이션 장애 등 어떤 상황에서도 작업 유실을 방지한다.
- 장애 복구 시 발생할 수 있는 **중복 발송**을 최소화하고, 제어(멱등성)할 수 있도록 설계한다.

---

## 2. 주요 구성 요소
- **Database (MySQL/InnoDB):** 발송 대상과 상태를 영구적으로 관리.
- **Queue (Redis):** 발송 작업을 워커에게 분배. 
- **Producer (Scheduler):** DB를 기반으로 발송할 작업을 생성하여 큐에 등록.
- **Consumer (Worker):** 큐의 작업을 가져와 실제 API를 호출하고 DB 상태를 갱신.

---

## 3. 핵심 DB 스키마 (Outbox Table)
모든 발송 작업은 API 호출 전 DB에 먼저 기록됩니다. (Transactional Outbox Pattern)

**`quiz_notification_log`**
| 컬럼명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | `BIGINT` | PK (AUTO_INCREMENT) |
| `user_id` | `BIGINT` | 알림 받을 사용자 ID (FK) |
| `quiz_data` | `JSON` | 발송할 메시지 내용 |
| `status` | `ENUM` | 작업 상태 (`PENDING`, `QUEUED`, `PROCESSING`, `SENT`, `FAILED`) |
| `created_at`| `DATETIME` | 작업 생성 시간 |
| `updated_at`| `DATETIME` | 상태 변경 시간 |

### ✏️ Status 정의
- **PENDING:** '오늘 발송할 대상'으로 최초 등록된 상태.
- **QUEUED:** 프로듀서가 Redis 큐 등록을 위해 DB에 선반영한 상태.
- **PROCESSING:** 워커가 큐에서 작업을 가져가 **API 호출을 시도 중**인 상태.
- **SENT:** API 호출 성공 및 DB 반영까지 완료된 최종 성공 상태.
- **FAILED:** 영구적인 오류로 재시도가 불가능한 상태. (DLQ로 격리)

---

## 4. 아키텍처 흐름
![Notification_Architecture.png](imgs/Notification_Architecture.png)

[1단계: 생산자 (Scheduler) 영역]

1. 별도의 스케줄러 스레드가 알림 발송 시간 10분 전에 User 테이블에서 알람 설정 한 유저 정보를 조회한다.

2. 유저 정보를 조회한 스케줄러는 오늘 알림 설정을 on 한 사용자들에 대해서만 quiz_notification_log 테이블을 생성하고 저장한다. (초기 status='PENDING')

3. 발송 시간이 되면 메인 스케줄러 스레드는 quiz_notification_log 테이블에서 오늘 작업들을 조회한다.

4. quiz_notification_log 테이블의 **status**를 'PENDING'에서 'QUEUED'로 변경한다.

- 트랜잭션 처리 함으로써 전부 QUEUED로 변경되도록 한다.

5. 트랜잭션으로 상태 변경('QUEUED')된 작업들을 레디스 큐(main-queue)에 푸시한다.

- 푸시 중간에 서버가 다운되면 보조 스케줄러가 돌면서 status='QUEUED'인데 10분이 지나도록 SENT로 바뀌지 않은 작업들을 찾아서 다시 큐에 푸시한다. (작업 유실 방지)

[2단계: 소비자 (Worker) 영역]

6. main-queue에서 worker-N-processing-queue(워커 전용 백업 큐)로 **BLMOVE**를 통해 원자적 이동을 실행한다.
- processing-queue에서는 작업의 모든 과정이 완료되어야만 해당 작업을 삭제한다.

7. 워커 스레드가 자신의 processing-queue에서 작업을 가져온다.

- processing-queue에서 작업을 가져오는 것은 **읽기(LRANGE 등)**로 처리한다.

- 삭제는 작업의 모든 과정이 완료된 후 마지막에 수행된다.

8. quiz_notification_log 테이블의 **status**를 'QUEUED'에서 'PROCESSING'으로 변경한다.

- (의미: 작업이 큐에서 꺼내져 처리가 시작되었음을 보장. API 호출 성공 여부는 보장 X)

9. 사용자에게 알람을 보내는 API를 호출한다.

10. API 응답 결과에 따라 작업을 분기 처리한다.

- 성공 시: quiz_notification_log 테이블의 **status**를 'PROCESSING'에서 'SENT'로 변경한다. (→ 11단계로 이동)

- 일시적 실패 시 (5xx 등): retry-queue로 (원자적 이동을 통해) 옮겨지며, retry 전문 워커 스레드에 의해서 재시도 된다.

- 영구적 실패 시 (4xx 등): dlq-queue로 (원자적 이동을 통해) 옮겨진다. 개발자가 수동으로 원인 분석 및 버그 수정을 해야 한다.

11. 최종 처리 후 processing-queue에서 작업을 삭제한다.

- DB **status**가 'SENT'로 상태 변경된 것을 확인한 후, **LREM**으로 processing-queue에서 해당 작업을 제거한다.
  
## 5. 구체적인 장애 시나리오
[스케줄러/레디스 큐 설계](https://long-feather-730.notion.site/2a0a987f6c4080148814dc9d57a63f90)
