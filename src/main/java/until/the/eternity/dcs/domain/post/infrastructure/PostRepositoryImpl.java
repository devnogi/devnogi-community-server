package until.the.eternity.dcs.domain.post.infrastructure;

import static until.the.eternity.dcs.domain.post.entity.QPost.post;
import static until.the.eternity.dcs.domain.post.entity.QPostMeta.postMeta;
import static until.the.eternity.dcs.domain.tag.entity.QPostTag.postTag;
import static until.the.eternity.dcs.domain.tag.entity.QTag.tag;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.entity.Post;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public Page<Post> findWithPostMetaByBoardId(Pageable pageable, Board board) {
        JPAQuery<Post> query =
                queryFactory
                        .selectFrom(post)
                        .leftJoin(postMeta)
                        .on(post.id.eq(postMeta.postId))
                        .where(
                                post.board.eq(board),
                                post.isDeleted.isFalse(),
                                post.isDraft.isFalse())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize());

        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (property) {
                case "likeCount":
                    query.orderBy(new OrderSpecifier<>(direction, postMeta.likeCount));
                    break;
                case "viewCount":
                    query.orderBy(new OrderSpecifier<>(direction, postMeta.viewCount));
                    break;
                default:
                    PathBuilder<Post> pathBuilder =
                            new PathBuilder<>(post.getType(), post.getMetadata());
                    query.orderBy(
                            new OrderSpecifier<>(
                                    direction, pathBuilder.get(property, Comparable.class)));
                    break;
            }
        }

        List<Post> content = query.fetch();

        JPAQuery<Long> countQuery =
                queryFactory
                        .select(post.count())
                        .from(post)
                        .where(
                                post.board.eq(board),
                                post.isDeleted.isFalse(),
                                post.isDraft.isFalse());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Post> findWithTagsById(Long id) {
        Post result =
                queryFactory
                        .selectFrom(post)
                        .leftJoin(post.postTags, postTag)
                        .fetchJoin()
                        .leftJoin(postTag.tag, tag)
                        .fetchJoin()
                        .where(post.id.eq(id), post.isDeleted.isFalse(), post.isDraft.isFalse())
                        .fetchOne();

        return Optional.ofNullable(result);
    }

    // todo
    @Override
    public Page<Post> findWithPostMetaByKeyword(Pageable pageable, String keyword) {
        String booleanQuery = toBooleanQuery(keyword);
        String orderBy = buildOrderBy(pageable);

        // 조회 쿼리
        String sql =
                """
			SELECT p.*,
				   MATCH(p.title, p.content) AGAINST (:q IN BOOLEAN MODE) AS score
			FROM post p
			LEFT JOIN post_meta pm ON pm.post_id = p.id
			WHERE p.is_deleted = 0
			  AND p.is_draft = 0
			  AND MATCH(p.title, p.content) AGAINST (:q IN BOOLEAN MODE)
			ORDER BY %s
			LIMIT :limit OFFSET :offset
			"""
                        .formatted(orderBy);

        // 카운트 쿼리
        String countSql =
                """
			SELECT COUNT(1)
			FROM post p
			WHERE p.is_deleted = 0
			  AND p.is_draft = 0
			  AND MATCH(p.title, p.content) AGAINST (:q IN BOOLEAN MODE)
			""";

        // 조회
        List<Post> content =
                em.createNativeQuery(sql, Post.class)
                        .setParameter("q", booleanQuery)
                        .setParameter("limit", pageable.getPageSize())
                        .setParameter("offset", (int) pageable.getOffset())
                        .getResultList();

        // 카운트
        Number total =
                (Number)
                        em.createNativeQuery(countSql)
                                .setParameter("q", booleanQuery)
                                .getSingleResult();

        return new PageImpl<>(content, pageable, total.longValue());
    }

    @Override
    public Page<Post> findWithPostMetaByBoardIdAndKeyword(
            Pageable pageable, Board board, String keyword) {
        String booleanQuery = toBooleanQuery(keyword);
        String orderBy = buildOrderBy(pageable);

        // 조회 쿼리
        String sql =
                """
				SELECT p.*,
				       MATCH(p.title, p.content) AGAINST (:q IN BOOLEAN MODE) AS score
				FROM post p
				LEFT JOIN post_meta pm ON pm.post_id = p.id
				WHERE p.board_id = :boardId
				  AND p.is_deleted = 0
				  AND p.is_draft = 0
				  AND MATCH(p.title, p.content) AGAINST (:q IN BOOLEAN MODE)
				ORDER BY %s
				LIMIT :limit OFFSET :offset
				"""
                        .formatted(orderBy);

        // 카운트 쿼리
        String countSql =
                """
				SELECT COUNT(1)
				FROM post p
				WHERE p.board_id = :boardId
				  AND p.is_deleted = 0
				  AND p.is_draft = 0
				  AND MATCH(p.title, p.content) AGAINST (:q IN BOOLEAN MODE)
				""";

        // 조회
        List<Post> content =
                em.createNativeQuery(sql, Post.class)
                        .setParameter("boardId", board.getId())
                        .setParameter("q", booleanQuery)
                        .setParameter("limit", pageable.getPageSize())
                        .setParameter("offset", (int) pageable.getOffset())
                        .getResultList();

        // 카운트
        Number total =
                (Number)
                        em.createNativeQuery(countSql)
                                .setParameter("boardId", board.getId())
                                .setParameter("q", booleanQuery)
                                .getSingleResult();

        return new PageImpl<>(content, pageable, total.longValue());
    }

    @Override
    public Page<Post> findWithPostMetaByUserId(Pageable pageable, Long userId) {
        JPAQuery<Post> query =
                queryFactory
                        .selectFrom(post)
                        .leftJoin(postMeta)
                        .on(post.id.eq(postMeta.postId))
                        .where(
                                post.userId.eq(userId),
                                post.isDeleted.isFalse(),
                                post.isDraft.isFalse())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize());

        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            switch (property) {
                case "createdAt":
                    query.orderBy(new OrderSpecifier<>(direction, post.createdAt));
                    break;
                default:
                    query.orderBy(new OrderSpecifier<>(Order.ASC, post.id));
            }
        }

        List<Post> content = query.fetch();

        Long count =
                queryFactory
                        .select(post.count())
                        .from(post)
                        .where(post.userId.eq((userId)))
                        .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0);
    }

    private String toBooleanQuery(String keyword) {
        if (keyword == null) return "";
        String[] tokens = keyword.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (!t.isBlank()) sb.append('+').append(t).append('*').append(' ');
        }
        return sb.toString().trim();
    }

    private String buildOrderBy(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return "score DESC, p.created_at DESC, p.id DESC";
        }
        List<String> parts = new ArrayList<>();
        pageable.getSort()
                .forEach(
                        o -> {
                            String col;
                            switch (o.getProperty()) {
                                case "createdAt" -> col = "p.created_at";
                                default -> col = "p.id";
                            }
                            parts.add(col + (o.isAscending() ? " ASC" : " DESC"));
                        });
        return String.join(", ", parts);
    }
}
