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
        Set<String> keys = scanKeys("postMeta:*");
        Set<String> filteredKeys =
                keys.stream().filter(key -> key.split(":").length == 2).collect(Collectors.toSet());

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
                postMetaToSave =
                        PostMeta.builder()
                                .postId(optionalPostMetaInDb.get().getPostId())
                                .likeCount(postMetaInRedis.getLikeCount())
                                .viewCount(postMetaInRedis.getViewCount())
                                .commentCount(postMetaInRedis.getCommentCount())
                                .build();
            } else {
                postMetaToSave = postMetaInRedis;
            }

            postMetaRepository.save(postMetaToSave);
        }
    }

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
