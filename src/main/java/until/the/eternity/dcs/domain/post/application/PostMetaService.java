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
    private static final long POST_META_LOCK_SECONDS = 10;
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
        redisTemplate.opsForHash().increment(key, VIEW_COUNT_FIELD, 1);

        redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);

        Long currentFieldCount = redisTemplate.opsForHash().size(key);

        if (currentFieldCount != null && currentFieldCount == 1) {

            String lockKey = generateLockKey(postId);

            Boolean iGotTheLock =
                    redisTemplate
                            .opsForValue()
                            .setIfAbsent(lockKey, "1", POST_META_LOCK_SECONDS, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(iGotTheLock)) {
                try {
                    PostMeta postMetaFromDb = getPostMetaFromDB(postId);

                    long dbViewCount = postMetaFromDb.getViewCount();

                    Object latestRedisViewObj =
                            redisTemplate.opsForHash().get(key, VIEW_COUNT_FIELD);
                    long latestRedisDelta =
                            (latestRedisViewObj != null)
                                    ? Long.parseLong(latestRedisViewObj.toString())
                                    : 0;
                    long correctViewCount = dbViewCount + latestRedisDelta;

                    Map<String, Object> dataToCache = postMetaFromDb.toMap();

                    dataToCache.put(VIEW_COUNT_FIELD, correctViewCount);

                    redisTemplate.opsForHash().putAll(key, dataToCache);

                    redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);

                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
        }
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

        redisTemplate.opsForHash().increment(key, LIKE_COUNT_FIELD, 1);

        redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);

        Long currentFieldCount = redisTemplate.opsForHash().size(key);

        if (currentFieldCount != null && currentFieldCount == 1) {

            String lockKey = generateLockKey(postId);

            Boolean iGotTheLock =
                    redisTemplate
                            .opsForValue()
                            .setIfAbsent(lockKey, "1", POST_META_LOCK_SECONDS, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(iGotTheLock)) {
                try {
                    PostMeta postMetaFromDb = getPostMetaFromDB(postId);

                    long dbLikeCount = postMetaFromDb.getLikeCount();

                    Object latestRedisLikesObj =
                            redisTemplate.opsForHash().get(key, LIKE_COUNT_FIELD);
                    long latestRedisDelta =
                            (latestRedisLikesObj != null)
                                    ? Long.parseLong(latestRedisLikesObj.toString())
                                    : 0;

                    long correctLikeCount = dbLikeCount + latestRedisDelta;

                    Map<String, Object> dataToCache = postMetaFromDb.toMap();

                    dataToCache.put(LIKE_COUNT_FIELD, correctLikeCount);

                    redisTemplate.opsForHash().putAll(key, dataToCache);

                    redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);

                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
        }
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

        redisTemplate.opsForHash().increment(key, LIKE_COUNT_FIELD, -1);

        redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);

        Long currentFieldCount = redisTemplate.opsForHash().size(key);

        if (currentFieldCount != null && currentFieldCount == 1) {

            String lockKey = generateLockKey(postId);

            Boolean iGotTheLock =
                    redisTemplate
                            .opsForValue()
                            .setIfAbsent(lockKey, "1", POST_META_LOCK_SECONDS, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(iGotTheLock)) {
                try {
                    PostMeta postMetaFromDb = getPostMetaFromDB(postId);

                    long dbLikeCount = postMetaFromDb.getLikeCount();

                    Object latestRedisLikesObj =
                            redisTemplate.opsForHash().get(key, LIKE_COUNT_FIELD);
                    long latestRedisDelta =
                            (latestRedisLikesObj != null)
                                    ? Long.parseLong(latestRedisLikesObj.toString())
                                    : 0;

                    long correctLikeCount = dbLikeCount + latestRedisDelta;

                    Map<String, Object> dataToCache = postMetaFromDb.toMap();

                    dataToCache.put(LIKE_COUNT_FIELD, correctLikeCount);

                    redisTemplate.opsForHash().putAll(key, dataToCache);

                    redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);

                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
        }
    }

    public void addComment(
            Long postId, String userIp) { // 댓글을 쓰고 repository에서 count하니까 당연히 두 개가 집계되지
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

    public PostMeta getPostMeta(Long postId) {
        String key = generateKey(postId);

        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> rawData = hashOperations.entries(key);

        if (rawData.size() == 4) {
            return objectMapper.convertValue(rawData, PostMeta.class);
        } else {
            PostMeta postMetaFromDb = getPostMetaFromDB(postId);

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

    private PostMeta getPostMetaFromDB(Long postId) {
        Integer commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
        return postMetaRepository
                .findById(postId)
                .orElseGet(() -> PostMeta.create(postId, commentCount));
    }
}
