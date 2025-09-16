package until.the.eternity.dcs.domain.post.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.enums.PostMetaType;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;

@Service
@RequiredArgsConstructor
public class PostMetaService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PostMetaRepository postMetaRepository;

    private String generateKey(Long postId) {
        return "postMeta:" + postId;
    }

    private String generateMethodKey(Long postId, String method, String userId) {
        return "postMeta:" + postId + ":" + method + ":" + userId;
    }

    public void viewPost(Long postId, String userIp) {
        String key = generateKey(postId);

        String methodKey = generateMethodKey(postId, PostMetaType.VIEW.getCode(), userIp);
        Object postMetaData = redisTemplate.opsForValue().get(key);

        System.out.println(methodKey);

        if (!canDoMethod(postId, PostMetaType.VIEW.getCode(), userIp)) {
            return;
        }

        PostMeta postMeta;
        if (postMetaData != null) {

            postMeta = objectMapper.convertValue(postMetaData, PostMeta.class);
            postMeta.viewPost();
        } else {

            postMeta = postMetaRepository.findById(postId).orElseGet(() -> PostMeta.create(postId));
            postMeta.viewPost();
        }

        redisTemplate.opsForValue().set(key, postMeta);
        redisTemplate.opsForValue().set(methodKey, postMeta, 1, TimeUnit.HOURS);
    }

    public void likePost(Long postId, String userIp) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.LIKE.getCode(), userIp);
        Object postMetaData = redisTemplate.opsForValue().get(key);

        if (!canDoMethod(postId, PostMetaType.LIKE.getCode(), userIp)) {
            return;
        }

        PostMeta postMeta;
        if (postMetaData != null) {
            postMeta = objectMapper.convertValue(postMetaData, PostMeta.class);
        } else {
            postMeta = postMetaRepository.findById(postId).orElseGet(() -> PostMeta.create(postId));
        }

        postMeta.like();
        redisTemplate.opsForValue().set(key, postMeta);
        redisTemplate.opsForValue().set(methodKey, postMeta, 12, TimeUnit.HOURS);
    }

    public void unlikePost(Long postId, String userIp) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.UNLIKE.getCode(), userIp);

        Object postMetaData = redisTemplate.opsForValue().get(key);

        if (!canDoMethod(postId, PostMetaType.UNLIKE.getCode(), userIp)) {
            return;
        }

        PostMeta postMeta;
        if (postMetaData != null) {
            postMeta = objectMapper.convertValue(postMetaData, PostMeta.class);
        } else {
            postMeta = postMetaRepository.findById(postId).orElseGet(() -> PostMeta.create(postId));
        }

        postMeta.unlike();
        redisTemplate.opsForValue().set(key, postMeta);
        redisTemplate.opsForValue().set(methodKey, postMeta, 12, TimeUnit.HOURS);
    }

    public void addComment(Long postId, String userIp) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.ADD_COMMENT.getCode(), userIp);

        Object postMetaData = redisTemplate.opsForValue().get(key);

        PostMeta postMeta;
        if (postMetaData != null) {
            postMeta = objectMapper.convertValue(postMetaData, PostMeta.class);
        } else {
            postMeta = postMetaRepository.findById(postId).orElseGet(() -> PostMeta.create(postId));
        }

        postMeta.addComment();
        redisTemplate.opsForValue().set(key, postMeta);
        redisTemplate.opsForValue().set(methodKey, postMeta);
    }

    public void deleteComment(Long postId, String userIp) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.DELETE_COMMENT.getCode(), userIp);

        Object postMetaData = redisTemplate.opsForValue().get(key);

        PostMeta postMeta;
        if (postMetaData != null) {
            postMeta = objectMapper.convertValue(postMetaData, PostMeta.class);
        } else {
            postMeta = postMetaRepository.findById(postId).orElseGet(() -> PostMeta.create(postId));
        }

        postMeta.addComment();
        redisTemplate.opsForValue().set(key, postMeta);
        redisTemplate.opsForValue().set(methodKey, postMeta);
    }

    public PostMeta getPostMeta(Long postId) {
        String key = generateKey(postId);
        // 1. Redis에서 데이터 조회 시도
        Object rawData = redisTemplate.opsForValue().get(key);

        if (rawData != null) {
            return objectMapper.convertValue(rawData, PostMeta.class);
        } else {

            PostMeta postMetaFromDb =
                    postMetaRepository.findById(postId).orElseGet(() -> PostMeta.create(postId));

            redisTemplate.opsForValue().set(key, postMetaFromDb);

            return postMetaFromDb;
        }
    }

    public boolean canDoMethod(Long postId, String method, String userIp) {
        String likeKey = generateMethodKey(postId, method, userIp);
        Object likeMetaInfo = redisTemplate.opsForValue().get(likeKey);
        return likeMetaInfo == null;
    }
}
