package until.the.eternity.dcs.domain.post.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
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

    public void viewPost(Long postId, String userIp) {

        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.VIEW.getCode(), userIp);

        Boolean isFirstTime =
                redisTemplate.opsForValue().setIfAbsent(methodKey, "1", 1, TimeUnit.HOURS);
        if (!isFirstTime) {
            return;
        }
        if (!redisTemplate.hasKey(key)) {
            setPostMeta(postId);
        }

        redisTemplate.opsForHash().increment(key, "viewCount", 1);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    public void likePost(Long postId, String userIp) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.LIKE.getCode(), userIp);

        Boolean isFirstTime =
                redisTemplate.opsForValue().setIfAbsent(methodKey, "1", 12, TimeUnit.HOURS);
        if (!isFirstTime) {
            return;
        }
        if (!redisTemplate.hasKey(key)) {
            setPostMeta(postId);
        }

        redisTemplate.opsForHash().increment(key, "likeCount", 1);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    public void unlikePost(Long postId, String userIp) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.UNLIKE.getCode(), userIp);

        Boolean isFirstTime =
                redisTemplate.opsForValue().setIfAbsent(methodKey, "1", 12, TimeUnit.HOURS);
        if (!isFirstTime) {
            return;
        }
        if (!redisTemplate.hasKey(key)) {
            setPostMeta(postId);
        }

        redisTemplate.opsForHash().increment(key, "likeCount", -1);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    public void addComment(Long postId, String userIp) {
        String key = generateKey(postId);
        if (!redisTemplate.hasKey(key)) {
            setPostMeta(postId);
        }

        redisTemplate.opsForHash().increment(key, "commentCount", 1);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    public void deleteComment(Long postId, String userIp) {
        String key = generateKey(postId);

        if (!redisTemplate.hasKey(key)) {
            setPostMeta(postId);
        }

        redisTemplate.opsForHash().increment(key, "commentCount", -1);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
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
}
