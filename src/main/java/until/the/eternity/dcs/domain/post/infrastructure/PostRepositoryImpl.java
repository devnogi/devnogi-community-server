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
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Page<Post> findWithPostMetaByBoardIdAndKeyword(
            Pageable pageable, Board board, String title, String content) {
        return null;
    }

    @Override
    public Page<Post> findWithPostMetaByUserId(Pageable pageable, Long userId) {
        return null;
    }
}
