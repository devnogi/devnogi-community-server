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

    // Redis Key를 생성하는 헬퍼 메서드
    private String generateKey(Long postId) {
        return "postMeta:" + postId;
    }

    private String generateMethodKey(Long postId, String method) {
        return "postMeta:" + postId + ":" + method;
    }

    public void viewPost(Long postId) {
        String key = generateKey(postId);

        String methodKey = generateMethodKey(postId, PostMetaType.VIEW.getCode());
        Object postMetaData = redisTemplate.opsForValue().get(key);

        if (!canDoMethod(postId, PostMetaType.VIEW.getCode())) {
            System.out.println("쿨타임");
            return;
        }

        PostMeta postMeta;
        if (postMetaData != null) {
            // 2-1. 데이터가 있으면 PostMeta 객체로 변환
            postMeta = objectMapper.convertValue(postMetaData, PostMeta.class);
            postMeta.viewPost();
        } else {
            // 2-2. 데이터가 없으면 새로 생성
            postMeta = postMetaRepository.findById(postId).orElseGet(() -> PostMeta.create(postId));
            postMeta.viewPost();
        }

        // 3. 변경된 객체를 다시 Redis에 저장 (JSON으로 직렬화됨)
        redisTemplate.opsForValue().set(key, postMeta);
        redisTemplate.opsForValue().set(methodKey, postMeta, 2, TimeUnit.MINUTES);
    }

    public void likePost(Long postId) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.LIKE.getCode());
        Object postMetaData = redisTemplate.opsForValue().get(key);

        if (!canDoMethod(postId, PostMetaType.LIKE.getCode())) {
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
        redisTemplate.opsForValue().set(methodKey, postMeta, 3, TimeUnit.MINUTES);
    }

    public void unlikePost(Long postId) {
        String key = generateKey(postId);
        String methodKey = generateMethodKey(postId, PostMetaType.UNLIKE.getCode());

        Object postMetaData = redisTemplate.opsForValue().get(key);

        if (!canDoMethod(postId, PostMetaType.UNLIKE.getCode())) {
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
        redisTemplate.opsForValue().set(methodKey, postMeta, 3, TimeUnit.MINUTES);
    }

    public PostMeta getPostMeta(Long postId) {
        String key = generateKey(postId);
        // 1. Redis에서 데이터 조회 시도
        Object rawData = redisTemplate.opsForValue().get(key);

        if (rawData != null) {
            // 2-1. Cache Hit: Redis에 데이터가 있으면 변환하여 반환
            return objectMapper.convertValue(rawData, PostMeta.class);
        } else {
            // 2-2. Cache Miss: RDB에서 데이터를 조회
            //      DB에도 없으면, 새로운 PostMeta 객체를 생성 (조회수 0, 좋아요 0 등)
            PostMeta postMetaFromDb =
                    postMetaRepository.findById(postId).orElseGet(() -> PostMeta.create(postId));

            // 3. RDB 조회 결과를 Redis에 저장 (다음 조회를 위해)
            redisTemplate.opsForValue().set(key, postMetaFromDb);

            return postMetaFromDb;
        }
    }

    public boolean canDoMethod(Long postId, String method) {
        String likeKey = generateMethodKey(postId, method);
        Object likeMetaInfo = redisTemplate.opsForValue().get(likeKey);
        return likeMetaInfo == null;
    }
}
