package until.the.eternity.dcs.domain.comment.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import until.the.eternity.dcs.common.config.CommentNotificationAsyncConfig;
import until.the.eternity.dcs.common.config.JpaConfig;
import until.the.eternity.dcs.common.config.QueryDslConfig;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.comment.application.CommentConverter;
import until.the.eternity.dcs.domain.comment.application.CommentCreatedNotificationListener;
import until.the.eternity.dcs.domain.comment.application.CommentLikeConverter;
import until.the.eternity.dcs.domain.comment.application.CommentPermissionEvaluator;
import until.the.eternity.dcs.domain.comment.application.CommentService;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.post.application.PostMetaService;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.user.application.UserSummaryService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@SpringBootTest(
        classes = CommentRedisFailureIntegrationTest.TestConfig.class,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:comment-redis-failure;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.flyway.enabled=false",
            "spring.docker.compose.enabled=false",
            "spring.data.redis.repositories.enabled=false",
            "app.notification.stream=test-notifications",
            "app.notification.maxLen=100"
        })
@AutoConfigureMockMvc(addFilters = false)
class CommentRedisFailureIntegrationTest {
    private static final long USER_ID = 42L;

    @Autowired MockMvc mockMvc;
    @Autowired CommentRepository commentRepository;
    @Autowired PostRepository postRepository;
    @Autowired PostMetaRepository postMetaRepository;
    @Autowired UserSummaryRepository userSummaryRepository;
    @Autowired RedisSender redisSender;

    private Long postId;

    @BeforeEach
    void setUp() {
        userSummaryRepository.save(
                UserSummary.builder().id(USER_ID).nickname("admin").grade(ADMIN).build());
        Post post =
                Post.builder()
                        .userId(7L)
                        .title("post")
                        .content("content")
                        .comments(new ArrayList<>())
                        .postTags(new ArrayList<>())
                        .build();
        post.setCreatedAt(LocalDateTime.now());
        postId = postRepository.save(post).getId();
        postMetaRepository.save(PostMeta.create(postId, 0));
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void redisFailureAfterCommitKeepsCreatedCommentAndDurableCount() throws Exception {
        doThrow(new RedisConnectionFailureException("redis unavailable"))
                .when(redisSender)
                .enqueue(any(NotificationJob.class));

        mockMvc.perform(
                        post("/api/comments/{postId}", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"parentComment\":null,\"content\":\"hello\"}"))
                .andExpect(status().isCreated());

        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(1);
        assertThat(postMetaRepository.findById(postId).orElseThrow().getCommentCount())
                .isEqualTo(1);
        verify(redisSender, timeout(2_000)).enqueue(any(NotificationJob.class));

        mockMvc.perform(delete("/api/comments/{id}", comments.getFirst().getId()))
                .andExpect(status().isNoContent());

        Comment deleted = commentRepository.findById(comments.getFirst().getId()).orElseThrow();
        assertThat(deleted.getIsDeleted()).isTrue();
        assertThat(postMetaRepository.findById(postId).orElseThrow().getCommentCount()).isZero();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableMethodSecurity
    @EntityScan(basePackages = "until.the.eternity.dcs.domain")
    @EnableJpaRepositories(
            basePackageClasses = {
                CommentRepository.class,
                PostRepository.class,
                UserSummaryRepository.class
            })
    @Import({
        QueryDslConfig.class,
        JpaConfig.class,
        CommentNotificationAsyncConfig.class,
        CommentController.class,
        CommentService.class,
        CommentConverter.class,
        CommentLikeConverter.class,
        CommentCreatedNotificationListener.class,
        PostMetaService.class
    })
    static class TestConfig {
        @Bean
        CommentPermissionEvaluator commentPermissionEvaluator(
                UserSummaryRepository userSummaryRepository, CommentRepository commentRepository) {
            return new CommentPermissionEvaluator(userSummaryRepository, commentRepository);
        }

        @Bean
        RedisSender redisSender() {
            return mock(RedisSender.class);
        }

        @Bean
        RedisTemplate<String, Object> redisTemplate() {
            return mock(RedisTemplate.class);
        }

        @Bean
        UserSummaryService userSummaryService() {
            return mock(UserSummaryService.class);
        }
    }
}
