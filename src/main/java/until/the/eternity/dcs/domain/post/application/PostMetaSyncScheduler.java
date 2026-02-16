package until.the.eternity.dcs.domain.post.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostMetaSyncScheduler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMetaRepository postMetaRepository;

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

            Long postId;
            try {
                postId = Long.parseLong(key.split(":")[1]);
            } catch (NumberFormatException e) {
                continue;
            }
            int viewsToAdd = parseIntFromMap(rawData, "viewCount");
            int likesToAdd = parseIntFromMap(rawData, "likeCount");
            int commentsToAdd = parseIntFromMap(rawData, "commentCount");

            Optional<PostMeta> optionalPostMetaInDb = postMetaRepository.findById(postId);

            PostMeta postMetaToSave;
            if (optionalPostMetaInDb.isPresent()) {
                PostMeta dbMeta = optionalPostMetaInDb.get();
                dbMeta.update(
                        dbMeta.getViewCount() + viewsToAdd,
                        dbMeta.getLikeCount() + likesToAdd,
                        dbMeta.getCommentCount() + commentsToAdd);
                postMetaToSave = dbMeta;
            } else {
                postMetaToSave =
                        PostMeta.builder()
                                .postId(postId)
                                .viewCount(viewsToAdd)
                                .likeCount(likesToAdd)
                                .commentCount(commentsToAdd)
                                .build();
            }
            postMetaRepository.save(postMetaToSave);

            if (viewsToAdd != 0) {
                redisTemplate.opsForHash().increment(key, "viewCount", -viewsToAdd);
            }
            if (likesToAdd != 0) {
                redisTemplate.opsForHash().increment(key, "likeCount", -likesToAdd);
            }
            if (commentsToAdd != 0) {
                redisTemplate.opsForHash().increment(key, "commentCount", -commentsToAdd);
            }
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
