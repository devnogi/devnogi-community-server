package until.the.eternity.dcs.domain.post.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.enums.PostMetaType;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;

@Service
@RequiredArgsConstructor
public class PostMetaService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PostMetaRepository postMetaRepository;
    private final CommentRepository commentRepository;

    private static final long IP_RATE_LIKE_LIMIT_HOURS = 12;
    private static final long IP_RATE_VIEW_LIMIT_HOURS = 1;
    private static final long POST_META_TTL_HOURS = 24;
    private static final String LIKE_COUNT_FIELD = "likeCount";
    private static final String VIEW_COUNT_FIELD = "viewCount";
    private static final String COMMENT_COUNT_FIELD = "commentCount";
    private static final String RATE_LIMIT_VALUE = "1";

    public void viewPost(Long postId, String userIp) {

        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.VIEW.getCode(), userIp);

        Boolean isFirstTime =
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                methodKey,
                                RATE_LIMIT_VALUE,
                                IP_RATE_VIEW_LIMIT_HOURS,
                                TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isFirstTime)) {
            return;
        }
        String lockKey = generateLockKey(postId);
        if (Boolean.TRUE.equals(setLocked(lockKey))) {
            try {
                if (!redisTemplate.hasKey(key)) {

                    setPostMeta(postId);
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            return;
        }
        redisTemplate.executePipelined(
                (RedisCallback<Object>)
                        connection -> {
                            connection
                                    .hashCommands()
                                    .hIncrBy(key.getBytes(), VIEW_COUNT_FIELD.getBytes(), 1);
                            connection
                                    .keyCommands()
                                    .expire(
                                            key.getBytes(),
                                            TimeUnit.HOURS.toSeconds(POST_META_TTL_HOURS));
                            return null;
                        });
    }

    public void likePost(Long postId, String userIp) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.LIKE.getCode(), userIp);

        Boolean isFirstTime =
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                methodKey,
                                RATE_LIMIT_VALUE,
                                IP_RATE_LIKE_LIMIT_HOURS,
                                TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isFirstTime)) {
            return;
        }
        String lockKey = generateLockKey(postId);
        if (Boolean.TRUE.equals(setLocked(lockKey))) {
            try {
                if (!redisTemplate.hasKey(key)) {

                    setPostMeta(postId);
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {

            return;
        }

        redisTemplate.executePipelined(
                (RedisCallback<Object>)
                        connection -> {
                            connection
                                    .hashCommands()
                                    .hIncrBy(key.getBytes(), LIKE_COUNT_FIELD.getBytes(), 1);
                            connection
                                    .keyCommands()
                                    .expire(
                                            key.getBytes(),
                                            TimeUnit.HOURS.toSeconds(POST_META_TTL_HOURS));
                            return null;
                        });
    }

    public void unlikePost(Long postId, String userIp) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.UNLIKE.getCode(), userIp);

        Boolean isFirstTime =
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                methodKey,
                                RATE_LIMIT_VALUE,
                                IP_RATE_LIKE_LIMIT_HOURS,
                                TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isFirstTime)) {
            return;
        }
        String lockKey = generateLockKey(postId);
        if (Boolean.TRUE.equals(setLocked(lockKey))) {
            try {
                if (!redisTemplate.hasKey(key)) {

                    setPostMeta(postId);
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {

            return;
        }

        redisTemplate.executePipelined(
                (RedisCallback<Object>)
                        connection -> {
                            connection
                                    .hashCommands()
                                    .hIncrBy(key.getBytes(), LIKE_COUNT_FIELD.getBytes(), -1);
                            connection
                                    .keyCommands()
                                    .expire(
                                            key.getBytes(),
                                            TimeUnit.HOURS.toSeconds(POST_META_TTL_HOURS));
                            return null;
                        });
    }

    public void addComment(Long postId, String userIp) {
        String key = generateKey(postId);
        String lockKey = generateLockKey(postId);
        if (Boolean.TRUE.equals(setLocked(lockKey))) {
            try {
                if (!redisTemplate.hasKey(key)) {

                    setPostMeta(postId);
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            return;
        }

        redisTemplate.executePipelined(
                (RedisCallback<Object>)
                        connection -> {
                            connection
                                    .hashCommands()
                                    .hIncrBy(key.getBytes(), COMMENT_COUNT_FIELD.getBytes(), 1);
                            connection
                                    .keyCommands()
                                    .expire(
                                            key.getBytes(),
                                            TimeUnit.HOURS.toSeconds(POST_META_TTL_HOURS));
                            return null;
                        });
    }

    public void deleteComment(Long postId, String userIp) {
        String key = generateKey(postId);
        String lockKey = generateLockKey(postId);
        if (Boolean.TRUE.equals(setLocked(lockKey))) {
            try {
                if (!redisTemplate.hasKey(key)) {

                    setPostMeta(postId);
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            return;
        }

        redisTemplate.executePipelined(
                (RedisCallback<Object>)
                        connection -> {
                            connection
                                    .hashCommands()
                                    .hIncrBy(key.getBytes(), COMMENT_COUNT_FIELD.getBytes(), -1);
                            connection
                                    .keyCommands()
                                    .expire(
                                            key.getBytes(),
                                            TimeUnit.HOURS.toSeconds(POST_META_TTL_HOURS));
                            return null;
                        });
    }

    public void likePost2(Long postId, String userIp) {
        String key = generateKey(postId); // 예: "post:meta:123"
        String methodKey = generateMethodKey(postId, PostMetaType.LIKE.getCode(), userIp);

        // 1. IP 기반 중복 '좋아요' 방지 (12시간)
        Boolean isFirstTime =
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                methodKey,
                                RATE_LIMIT_VALUE,
                                IP_RATE_LIKE_LIMIT_HOURS,
                                TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isFirstTime)) {
            return; // 중복 클릭
        }

        // 2. [핵심 1] '좋아요' 즉시 반영 (누락 방지)
        //    - key가 만료되었다면 이 시점에 'likeCount: 1'로 새로 생성됩니다.
        //    - 이 값을 '캐시 만료 후 쌓인 값 (delta)'로 간주합니다.
        long currentRedisLikes = redisTemplate.opsForHash().increment(key, LIKE_COUNT_FIELD, 1);

        // 3. TTL 갱신
        redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);

        // 4. 이 key가 방금 'likeCount'만 가지고 새로 생성된 것인지 확인
        //    - currentRedisLikes == 1 만으로는 부족합니다.
        //      (기존 0 -> 1이 된 것일 수도 있으므로)
        //    - [수정] size()를 체크하는 것이 더 명확합니다.
        System.out.println("postMeta 초기값: " + redisTemplate.opsForHash().entries(key).toString());
        Long currentFieldCount = redisTemplate.opsForHash().size(key);

        if (currentFieldCount != null && currentFieldCount == 1) {
            // 5. 'likeCount' 필드만 존재 -> 캐시 만료로 인한 "초기화 작업" 필요

            String lockKey = generateLockKey(postId) + ":init"; // 초기화 전용 락
            // 10초짜리 락 획득 시도 (초기화 작업 타임아웃)
            Boolean iGotTheLock =
                    redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(iGotTheLock)) {
                // 6. 내가 락을 획득했다! (내가 Initializer)
                try {
                    // [핵심 2] '카운트 리셋' 문제 해결

                    // 7. DB에서 원본 데이터 로드
                    //    (이 시점에 commentCount도 같이 가져오는 것이 효율적입니다.)
                    Integer commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
                    PostMeta postMetaFromDb =
                            postMetaRepository
                                    .findById(postId)
                                    .orElseGet(() -> PostMeta.create(postId, commentCount));

                    // 8. DB의 '좋아요' 카운트 (베이스)
                    long dbLikeCount = postMetaFromDb.getLikeCount(); // (가정)

                    // 9. [중요] '좋아요' 카운트 보정
                    //    - 내가 락을 기다리는 동안 다른 사용자가 '좋아요'를 더 눌렀을 수 있습니다.
                    //    - 가장 최신 Redis 값을 다시 읽어옵니다.
                    Object latestRedisLikesObj =
                            redisTemplate.opsForHash().get(key, LIKE_COUNT_FIELD);
                    long latestRedisDelta =
                            (latestRedisLikesObj != null)
                                    ? Long.parseLong(latestRedisLikesObj.toString())
                                    : 0;

                    //    - 올바른 값 = DB 베이스 값 + 캐시 만료 후 Redis에 쌓인 값
                    long correctLikeCount = dbLikeCount + latestRedisDelta;

                    // 10. Redis에 저장할 전체 맵 생성
                    Map<String, Object> dataToCache = postMetaFromDb.toMap();

                    // 11. 보정된 '좋아요' 카운트와 최신 댓글 수로 덮어쓰기
                    dataToCache.put(LIKE_COUNT_FIELD, correctLikeCount);
                    dataToCache.put("commentCount", commentCount); // (가정)
                    // ... (다른 필드들 설정) ...

                    // 12. Redis에 '전체 데이터' 덮어쓰기 (putAll)
                    redisTemplate.opsForHash().putAll(key, dataToCache);

                    // 13. TTL 재설정 (putAll로 인해 TTL이 사라졌을 수 있음)
                    redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);

                } finally {
                    // 14. 락 해제
                    redisTemplate.delete(lockKey);
                }
            }
            // 15. (else) 내가 락 획득에 실패했다면?
            //     - 다른 스레드가 6~14번을 수행 중(또는 완료)이라는 의미.
            //     - 내 '좋아요'는 2번에서 이미 반영되었고, 락을 가진 스레드가 보정해줄 것임.
            //     - 나는 아무것도 안 하고 종료. (Exception 아님, 정상 종료임)
        }
        // 16. (else) currentFieldCount > 1 이라면?
        //     - 캐시가 유효한 상태였음 (모든 필드가 존재함).
        //     - 2번에서 '좋아요'가 정상적으로 +1 되었음.
        //     - 나는 아무것도 안 하고 종료.
    }

    public PostMeta getPostMeta(Long postId) {
        String key = generateKey(postId);

        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> rawData = hashOperations.entries(key);

        if (!rawData.isEmpty()) {
            return objectMapper.convertValue(rawData, PostMeta.class);
        } else {
            Integer commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
            PostMeta postMetaFromDb =
                    postMetaRepository
                            .findById(postId)
                            .orElseGet(() -> PostMeta.create(postId, commentCount));

            hashOperations.putAll(key, postMetaFromDb.toMap());
            return postMetaFromDb;
        }
    }

    public boolean canDoMethod(Long postId, String method, String userIp) {
        String likeKey = generateMethodKey(postId, method, userIp);
        Object likeMetaInfo = redisTemplate.opsForValue().get(likeKey);
        return likeMetaInfo == null;
    }

    public void deletePostMeta(Long postId) {
        String key = generateKey(postId);
        Set<String> keySet = redisTemplate.keys(key + "*");
        if (keySet.isEmpty()) {
            return;
        }
        redisTemplate.delete(keySet);
    }

    private String generateKey(Long postId) {
        return "postMeta:" + postId;
    }

    private String generateMethodKey(Long postId, String method, String userId) {
        return "postMeta:" + postId + ":" + method + ":" + userId;
    }

    private String generateLockKey(Long postId) {
        return "postMeta:" + postId + ":" + "lock";
    }

    private void setPostMeta(Long postId) {
        String key = generateKey(postId);

        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();

        Integer commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
        PostMeta postMetaFromDb =
                postMetaRepository
                        .findById(postId)
                        .orElseGet(() -> PostMeta.create(postId, commentCount));

        hashOperations.putAll(key, postMetaFromDb.toMap());
    }

    private Boolean setLocked(String lockKey) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 15, TimeUnit.SECONDS);
    }
}
