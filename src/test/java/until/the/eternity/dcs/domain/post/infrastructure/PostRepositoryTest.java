package until.the.eternity.dcs.domain.post.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import until.the.eternity.dcs.common.config.QueryDslConfig;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;

@DataJpaTest
@Import(QueryDslConfig.class)
public class PostRepositoryTest {

    @Autowired PostRepository postRepository;
    @Autowired EntityManager em;

    private Board board;

    @BeforeEach
    void setup() {
        // 1. 게시판 저장
        board = Board.builder().name("testBoard").topCategory("test").subCategory("test").build();
        board.setCreatedAt(LocalDateTime.now());
        em.persist(board); // DB 저장 완료
    }

    @Test
    @DisplayName("인기글 랭킹 로직 검증: (조회수 + 댓글*3) - (경과시간*2)")
    void verifyPopularPostRanking() {
        // Given: 데이터 준비 (저장 로직 포함)

        // 1위 후보 (130점): Post A (최신, 조회수100, 댓글10)
        Post postA = createPost("Post A", 100, 10);

        // 2위 후보 (100점): Post B (반응 좋지만 오래됨)
        Post postB = createPost("Post B", 200, 20);
        // [중요] DB에 저장된 시간을 강제로 과거(80시간 전)로 변경
        updateCreatedAt(postB.getId(), LocalDateTime.now().minusHours(80));

        // 3위 후보 (0점): Post C (최신, 반응 없음)
        Post postC = createPost("Post C", 0, 0);

        // [중요] 영속성 컨텍스트 초기화 (DB와 싱크 맞추기)
        em.flush();
        em.clear();

        // When: 조회
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findPopularPostsByBoardId(pageable, this.board);

        // Then: 검증
        List<Post> content = result.getContent();

        // 1. 개수 확인
        assertThat(content).hasSize(3);

        // 2. 순서 확인 (제목도 일치시킴)
        assertThat(content.get(0).getTitle()).isEqualTo("Post A");
        assertThat(content.get(1).getTitle()).isEqualTo("Post B");
        assertThat(content.get(2).getTitle()).isEqualTo("Post C");

        // 출력 확인
        content.forEach(p -> System.out.println("제목: " + p.getTitle()));
    }

    // --- 데이터를 쉽게 만들기 위한 헬퍼 메서드 ---
    private Post createPost(String title, int viewCount, int commentCount) {
        // 1. Post 객체 생성
        Post post =
                Post.builder()
                        .board(this.board) // 위에서 만든 board 사용
                        .title(title)
                        .content("Content")
                        .userId(1L)
                        .isDraft(false)
                        .isBlocked(false)
                        .comments(new ArrayList<>())
                        .postTags(new ArrayList<>())
                        .build();
        post.setIsDeleted(false);
        post.setCreatedAt(LocalDateTime.now());
        // 2. [필수] Post DB 저장 (이때 ID가 생성됨)
        em.persist(post);

        // 3. PostMeta 생성 (위에서 생성된 post.getId() 사용)
        PostMeta meta =
                PostMeta.builder()
                        .postId(post.getId()) // [중요] 실제 ID 연결
                        .viewCount(viewCount)
                        .commentCount(commentCount)
                        .likeCount(0)
                        .build();

        // 4. [필수] Meta DB 저장
        em.persist(meta);

        return post;
    }

    // 시간 조작용 메서드
    private void updateCreatedAt(Long postId, LocalDateTime pastDate) {
        em.createNativeQuery("UPDATE post SET created_at = :date WHERE id = :id")
                .setParameter("date", pastDate)
                .setParameter("id", postId)
                .executeUpdate();
    }
}
