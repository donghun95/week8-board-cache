# week8-board-cache

8주차 Redis 기초 + 캐시 전략 수업용 게시판 데모입니다.

7주차 JPA 전용 프로젝트 `02-week7-board-jpa`를 복사한 뒤 Redis Cache와 Redis 조회수 카운터를 추가했습니다. 패키지명은 수업 범위 축소를 위해 기존 `com.dsa.week5board`를 유지합니다.

## 목적

- 인기 게시글 목록을 Spring Cache + Redis로 캐싱합니다.
- 첫 조회는 DB SELECT, 두 번째 조회는 Redis hit로 SQL이 나가지 않는 흐름을 확인합니다.
- 게시글 생성/수정/삭제/조회수 flush 시 인기 게시글 캐시를 무효화합니다.
- 조회수 증가는 Redis `INCR`로 누적하고, DB에는 `GETDEL` + JPQL update로 일괄 반영합니다.

## 주요 파일<img width="1160" height="104" alt="스크린샷 2026-06-20 160856" src="https://github.com/user-attachments/assets/968dc325-ab64-407b-8d26-a419b74f1b3a" />


- `src/main/java/com/dsa/week5board/config/RedisCacheConfig.java`
- `src/main/java/com/dsa/week5board/board/service/BoardCacheService.java`
- `src/main/java/com/dsa/week5board/board/service/BoardViewCountService.java`
- `src/main/java/com/dsa/week5board/board/repository/BoardRepository.java`
- `src/main/java/com/dsa/week5board/board/controller/BoardController.java`
- `src/main/java/com/dsa/week5board/board/controller/AdminViewCountController.java`
- `requests.http`
- `docs/week8-mentor-final.md`

## 실행 준비

Redis를 로컬에서 설치하려고 하니 3.0.45 버전이 최신이어서   
이번 수업에서는 6.2 이상이 되어야 getAndDelet를 사용하기에   
로컬에서 msi로 설치 하였다가 레디스 클라우드로 변경했습니다.  
레디스 클라우드에서 호스트, 패스워드, 포트를 잘 기억한 후에   
redis insight에 연결해서 확인 해보았습니다.

```

## 확인 요청

`requests.http`를 열고 위에서부터 실행합니다.

- `GET /api/boards/popular`
- `GET /api/boards/popular` 두 번째 호출
- `POST /api/boards`
- `POST /api/boards/1/views/redis`
- `GET /api/boards/1/views/redis`
- `POST /api/admin/views/flush`
- `GET /api/boards/1`

## 비교 포인트
- 첫 번째 인기 게시글 조회는 p6spy SQL 로그가 나갑니다.
- 두 번째 인기 게시글 조회는 Redis cache hit라 SQL이 나가지 않습니다.
- 등록 직후 키 삭제 → 다음 GET에서 재생성시 캐시가 무효화 됩니다.
- 조회수: Redis INCR 방식을 확인합니다.
- Redis key는 `week8:board:popular::top10` 형태입니다.
- 조회수 Redis counter key는 `week8:board:views:{id}` 형태입니다.
- flush 후 Redis 누적값은 사라지고 DB `views`가 증가합니다.

```

비교 포인트 1번과 2번 내용
get 으로 조회를 연달아 했을때 첫번째에서는 redis에 저장을 시키고  
두번째 조회시 Redis hit으로 sql 조회 없이 결과값을 보여준다.
<img width="1160" height="104" alt="스크린샷 2026-06-20 160856" src="https://github.com/user-attachments/assets/3c87e2f9-cf81-4db0-ad68-1acfe041a96b" />

비교 포인트 3번 내용
새글 등록시 캐시는 무효화 되고 조회시 DB를 재조회 하는 것을 확인 할 수 있다.
<img width="1166" height="191" alt="스크린샷 2026-06-20 164701" src="https://github.com/user-attachments/assets/38803650-dd61-4881-9954-670cd923b1e4" />


비교 포인트 4번 내용
opsForValue().increment() 한 줄로 Redis INCR 명령이 나감. 원자적이라 여러 요청이
동시에 들어와도 lost update 없음 (5주차 ViewCountRaceDemo 의 lost update가 여기서는
발생 X)
<img width="1207" height="289" alt="스크린샷 2026-06-20 161259" src="https://github.com/user-attachments/assets/f334d9d9-72f7-40fa-bc06-b9b26546a2ac" />
비교 포인트 7번 내용

getAndDelete() 는 Redis GETDEL   
Redis 6.2 이상에서 지원. 값을 가져오면서 키 삭제를
한 명령으로 처리. GET 후 DEL 두 명령 사이에 다른 INCR이 끼면 증가분이 사라지지만
GETDEL 은 그게 없습니다.
DB 반영은 views = views + delta . 누적값 덮어쓰기 절대 X.
flush 후 인기 캐시 evict  
Redis 카운터와 인기 게시글 캐시의 일관성을 맞춰주는 부분.
<img width="1177" height="163" alt="스크린샷 2026-06-20 161453" src="https://github.com/user-attachments/assets/654fe79f-a71b-45a5-b7cd-062024eddf9d" />

## 내용 정리
Cache-Aside / Write-Through / Write-Behind 차이
- Cache-Aside : 데이터를 읽을 때 먼저 캐시를 보고, 없으면 그때서야 DB에서 가져와 캐시에 저장하는 방식입니다.
- 장점 : 실제로 사용되는 데이터만 캐시에 올라가므로 메모리가 효율적입니다.
- 단점 : 처음 접근하는 데이터는 무조건 캐시 미스가 발생해서 초기 조회 속도가 느릴 수 있습니다.
- Write-Through : 데이터를 새로 저장하거나 수정할 때, 캐시와 DB에 동시에 똑같이 바로바로 적어 두는 방식입니다.
- 장점 : 캐시와 DB의 데이터가 항상 100% 일치하므로 데이터 일관성이 완벽합니다.
- 단점 : 매번 저장할 때마다 두곳에 다 써야 하므로 쓰기 속도가 상대적으로 느립니다.
- Write-Behind : 쓰기 요청이 오면 일단 캐시에만 빠르게 적어두고, DB 반영은 일정 주기로 모아서 비동기로 처리하는 방식입니다.
  이번 8주자 과제의 조회수 카운터 구현 방식이 바로 이 패턴입니다. 조회수가 오를 때마다 DB UPDATE를 치지 않고 Redis INCR로 모았다가 5분마다 한 번에 DB에 넣는 방식입니다.
- 장점 : 쓰기 성능이 세가지 패턴 중 가장 압도적으로 빠릅니다.
- 단점 : 캐시 메모리에만 저장이 되어 있는 상태에서 Redis 서버가 갑자기 다운되면 아직 DB에 반영되지 못한 최신 데이터가 유실될 위험이 있다.

## 면접 답변 정리

```
캐시 무효화 전략 — TTL, Eviction, 일관성 문제 방어

캐시 데이터는 언제든 실제 DB와 달라질 수 있으므로(Stale 데이터), 일관성을 방어하기 위해 두 가지 장치를 섞어 씁니다.

TTL (Time To Live): 키마다 만료 시간을 설정하는 방식입니다. 예를 들어 인기 게시글 캐시에 TTL을 30초로 두면, 데이터 일관성이 깨지더라도 "최악의 경우 30초 동안만 stale 데이터가 나간다"는 시간적 마지노선을 보장합니다.

Eviction (명시적 삭제): 데이터의 변경 이벤트(CUD - 등록, 수정, 삭제)가 발생했을 때 관련 캐시 키를 즉시 지워버리는 방식입니다. Spring의 @CacheEvict가 이에 해당합니다.

일관성 문제 방어: 캐시 일관성을 100% 완벽하게 보장하기는 구조적으로 어렵습니다. 따라서 보통 Cache-Aside + TTL + Eviction을 조합하여 동기화 공백을 최소화합니다. 최종 신뢰 소스(Single Source of Truth)는 항상 DB에 두고, 캐시는 오직 성능 최적화 레이어로만 바라보아야 합니다.

Redis INCR로 조회수 처리하는 이유

동일한 게시글에 수많은 사용자가 동시에 접근하여 조회수를 올릴 때 발생할 수 있는 동시성(Concurrency) 이슈를 해결하기 위함입니다.

원자성(Atomicity) 보장: Redis는 단일 스레드(Single-threaded) 기반으로 명령을 하나씩 순차적으로 처리합니다. INCR 명령은 원자적으로 수행되므로 여러 요청이 동시에 들어와도 데이터가 꼬이거나 유실(Race Condition)되지 않고 정확히 1씩 증가합니다.

DB 부하 분산: 일반적인 관계형 DB(RDBMS)에서 UPDATE board SET views = views + 1을 때리면 행(Row)에 락이 걸리고 대량 트래픽 상황에서 DB 병목의 주원인이 됩니다. Redis 메모리 상에서 INCR로 숫자를 빠르게 올린 뒤, 일정 주기마다 DB에 한 번에 반영(Write-Behind 패턴)하면 DB 부담을 획기적으로 줄일 수 있습니다.

 KEYS vs SCAN 의 차이와 SCAN 도 완벽한 안전장치가 아닌 이유

Redis 운영 환경에서 모든 키를 탐색할 때 사용하는 두 명령의 차이점입니다.

KEYS

동작: 모든 메모리를 뒤져 조건에 맞는 키를 한 번에 다 가져옵니다.

문제점: Redis는 단일 스레드로 일하므로, KEYS 명령이 실행되는 동안 다른 모든 GET, SET 같은 서비스 요청이 블로킹(대기) 상태가 됩니다. 데이터가 많으면 서비스 서버가 뻗는 치명적인 장애를 유발하므로 운영 환경에서는 절대 금지됩니다.

SCAN

동작: 커서(Cursor) 기반으로 데이터를 조금씩 나누어 조회(Paging)합니다. 한 번 실행할 때 아주 짧은 시간만 점유하므로 다른 요청을 방해하지 않아 안전합니다.

SCAN도 완벽한 안전장치가 아닌 이유 (한계):

데이터 누락/중복 가능성: SCAN이 수행되는 도중(첫 번째 커서부터 마지막 커서까지 도는 사이)에 새로운 키가 추가되거나 기존 키가 삭제·변경되면, 전체 순회 과정에서 특정 키가 두 번 호출(중복)되거나 아예 호출되지 않는(누락) 현상이 발생할 수 있습니다.

따라서 SCAN은 실시간 서비스에 영향을 주지 않는 안전한 탐색 도구일 뿐, 순회 시점의 완벽한 데이터 스냅샷을 보장하지는 않습니다.


Cache Stampede가 뭐고 어떻게 막나

대규모 트래픽이 몰리는 인기 게시글의 캐시 키가 TTL 만료 등으로 인해 한순간에 사라졌을 때, 수많은 요청이 동시에 캐시 미스(Miss)를 겪고 일제히 DB로 조회를 요청하는 현상입니다. 이를 Dogpile 문제라고도 부르며, 이 순간 DB 커넥션이 마비되거나 서버가 다운될 수 있습니다.

해결 방식:

Jitter (만료 시간 무작위화): 캐시 만료 시간(TTL)을 설정할 때 고정값(예: 딱 30초) 대신 약간의 랜덤 값(예: 30초 ~ 35초 사이)을 섞어 줍니다. 여러 키가 동시에 파괴되는 것을 막아 줍니다.

Mutex Lock / 분산 락: 캐시 미스가 났을 때 딱 하나의 요청(락을 획득한 녀석)만 DB에 접근해 캐시를 갱신하게 하고, 나머지 요청은 잠깐 대기했다가 캐시 채워진 것을 보고 타도록 제어합니다. (단일 JVM 내에서는 Spring @Cacheable(sync = true) 옵션으로 방어가 가능하며, 멀티 서버 환경 구조라면 Redis를 이용한 분산 락이 필요합니다.)

백그라운드 선갱신 (Ahead-of-time 갱신): 만료 시간이 되기 전에 별도의 스케줄러나 비동기 스레드가 캐시를 미리 조회해서 갱신해 두는 방식입니다.
```







