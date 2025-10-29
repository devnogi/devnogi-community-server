package until.the.eternity.dcs.domain.post.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;

@Component
@RequiredArgsConstructor
public class PostMetaSyncScheduler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMetaRepository postMetaRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void syncPostMetaToDb() {
        // 1. "postMeta:*" 패턴의 모든 키를 SCAN 명령어로 안전하게 조회
        Set<String> keys = scanKeys("postMeta:*");
        Set<String> filteredKeys =
                keys.stream()
                        .filter(key -> key.split(":").length == 2) // ✨ 핵심 필터링 로직
                        .collect(Collectors.toSet());

        for (String key : filteredKeys) {
            Map<Object, Object> rawData = redisTemplate.opsForHash().entries(key);
            if (rawData.isEmpty()) {
                continue;
            }

            PostMeta postMetaInRedis = objectMapper.convertValue(rawData, PostMeta.class);

            Optional<PostMeta> optionalPostMetaInDb =
                    postMetaRepository.findById(postMetaInRedis.getPostId());

            PostMeta postMetaToSave;
            if (optionalPostMetaInDb.isPresent()) {
                // 2-1. [UPDATE] DB에 데이터가 있으면, Builder로 새 객체를 만듭니다.
                // ID는 기존 엔티티의 것을 그대로 사용합니다.
                postMetaToSave =
                        PostMeta.builder()
                                .postId(optionalPostMetaInDb.get().getPostId()) // 기존 ID 사용
                                .likeCount(postMetaInRedis.getLikeCount()) // Redis 값으로 업데이트
                                .viewCount(postMetaInRedis.getViewCount()) // Redis 값으로 업데이트
                                .commentCount(postMetaInRedis.getCommentCount()) // Redis 값으로 업데이트
                                .build();
            } else {
                // 2-2. [INSERT] DB에 데이터가 없으면, Redis에 있던 객체를 그대로 사용합니다.
                postMetaToSave = postMetaInRedis;
            }

            // 3. JPA save 실행 (ID 존재 여부에 따라 INSERT 또는 UPDATE 실행)
            postMetaRepository.save(postMetaToSave);
        }
    }

    /** SCAN을 사용하여 패턴에 맞는 키를 안전하게 조회하는 메서드 */
    private Set<String> scanKeys(String pattern) {
        return redisTemplate.execute(
                (RedisCallback<Set<String>>)
                        connection -> {
                            Set<String> keys = new HashSet<>();
                            Cursor<byte[]> cursor =
                                    connection.scan(
                                            ScanOptions.scanOptions()
                                                    .match(pattern)
                                                    .count(1000)
                                                    .build());
                            while (cursor.hasNext()) {
                                keys.add(new String(cursor.next()));
                            }
                            return keys;
                        });
    }
}
