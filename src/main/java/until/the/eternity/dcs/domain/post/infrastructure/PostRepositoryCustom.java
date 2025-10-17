package until.the.eternity.dcs.domain.post.infrastructure;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.entity.Post;

public interface PostRepositoryCustom {
    Page<Post> findWithPostMetaByBoardId(Pageable pageable, Board board);

    Optional<Post> findWithTagsById(Long id);

    Page<Post> findWithPostMetaByKeyword(Pageable pageable, String keyword);

    Page<Post> findWithPostMetaByBoardIdAndKeyword(Pageable pageable, Board board, String keyword);

    Page<Post> findWithPostMetaByUserId(Pageable pageable, Long userId);
}
