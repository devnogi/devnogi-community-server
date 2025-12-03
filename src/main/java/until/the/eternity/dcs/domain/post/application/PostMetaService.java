package until.the.eternity.dcs.domain.post.application;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.enums.PostMetaType;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostMetaService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMetaRepository postMetaRepository;
    private final CommentRepository commentRepository;

    private static final long IP_RATE_LIKE_LIMIT_HOURS = 12;
    private static final long IP_RATE_VIEW_LIMIT_HOURS = 1;
    private static final long POST_META_TTL_HOURS = 24;
    private static final String LIKE_COUNT_FIELD = "likeCount";
    private static final String VIEW_COUNT_FIELD = "viewCount";
    private static final String COMMENT_COUNT_FIELD = "commentCount";
    private static final String RATE_LIMIT_VALUE = "1";

    @Transactional
    public void createPostMeta(Long postId) {
        PostMeta pm = PostMeta.create(postId, 0);
        postMetaRepository.save(pm);
    }

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
    }

    public void addComment(Long postId) {
        String key = generateKey(postId);

        redisTemplate.opsForHash().increment(key, COMMENT_COUNT_FIELD, 1);

        redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);
    }

    public void deleteComment(Long postId) {
        String key = generateKey(postId);
        redisTemplate.opsForHash().increment(key, COMMENT_COUNT_FIELD, -1);

        redisTemplate.expire(key, POST_META_TTL_HOURS, TimeUnit.HOURS);
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

    public PostMetaResponse getPostMetaInfo(Long postId) {
        PostMeta postMeta =
                postMetaRepository
                        .findByPostId(postId)
                        .orElse(
                                PostMeta.create(
                                        postId,
                                        commentRepository.countByPostIdAndIsDeletedFalse(postId)));

        String key = generateKey(postId);
        Map<Object, Object> postMetaInRedis = redisTemplate.opsForHash().entries(key);
        int viewsToAdd = parseIntFromMap(postMetaInRedis, VIEW_COUNT_FIELD);
        int likesToAdd = parseIntFromMap(postMetaInRedis, LIKE_COUNT_FIELD);
        int commentsToAdd = parseIntFromMap(postMetaInRedis, COMMENT_COUNT_FIELD);

        return PostMetaResponse.of(postMeta, viewsToAdd, likesToAdd, commentsToAdd);
    }

    public Map<Long, PostMetaResponse> getPostMetaInfos(List<Long> postIdList) {

        List<PostMeta> dbMetas = postMetaRepository.findAllByPostIdIn(postIdList);

        Map<Long, PostMeta> dbMetaMap =
                dbMetas.stream().collect(Collectors.toMap(PostMeta::getPostId, meta -> meta));

        List<Object> pipelineResults =
                redisTemplate.executePipelined(
                        new SessionCallback<Object>() {
                            @Override
                            public <K, V> Object execute(RedisOperations<K, V> operations) {
                                for (Long postId : postIdList) {
                                    String key = generateKey(postId);
                                    operations.opsForHash().entries((K) key);
                                }
                                return null;
                            }
                        });

        Map<Long, PostMetaResponse> resultMap = new HashMap<>();

        for (int i = 0; i < postIdList.size(); i++) {
            Long postId = postIdList.get(i);

            PostMeta postMeta = dbMetaMap.get(postId);

            if (postMeta == null) {
                continue;
            }

            Map<Object, Object> redisData = (Map<Object, Object>) pipelineResults.get(i);

            PostMetaResponse postMetaResponse = PostMetaResponse.from(postMeta);

            if (redisData != null && !redisData.isEmpty()) {
                postMetaResponse =
                        PostMetaResponse.of(
                                postMeta,
                                parseIntFromMap(redisData, VIEW_COUNT_FIELD),
                                parseIntFromMap(redisData, LIKE_COUNT_FIELD),
                                parseIntFromMap(redisData, COMMENT_COUNT_FIELD));
            }

            resultMap.put(postId, postMetaResponse);
        }

        return resultMap;
    }

    private String generateKey(Long postId) {
        return "postMeta:" + postId;
    }

    private String generateMethodKey(Long postId, String method, String userId) {
        return "postMeta:" + postId + ":" + method + ":" + userId;
    }

    private int parseIntFromMap(Map<Object, Object> map, String field) {
        Object value = map.get(field);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
