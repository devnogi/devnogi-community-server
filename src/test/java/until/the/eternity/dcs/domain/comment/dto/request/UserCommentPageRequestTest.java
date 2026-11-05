package until.the.eternity.dcs.domain.comment.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import until.the.eternity.dcs.common.exception.InvalidPageRequestException;

class UserCommentPageRequestTest {
    @Test
    void defaultsToFirstPageWithTwentyItemsAndStableDescendingSort() {
        Pageable pageable = new UserCommentPageRequest(null, null).toPageable();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        assertThat(pageable.getSort().getOrderFor("id").isDescending()).isTrue();
    }

    @Test
    void acceptsOneAndFiftyAsSizeBoundaries() {
        assertThat(new UserCommentPageRequest(1, 1).toPageable().getPageSize()).isEqualTo(1);
        assertThat(new UserCommentPageRequest(1, 50).toPageable().getPageSize()).isEqualTo(50);
    }

    @Test
    void rejectsInvalidPageAndSize() {
        assertThatThrownBy(() -> new UserCommentPageRequest(0, 20).toPageable())
                .isInstanceOf(InvalidPageRequestException.class);
        assertThatThrownBy(() -> new UserCommentPageRequest(1, 0).toPageable())
                .isInstanceOf(InvalidPageRequestException.class);
        assertThatThrownBy(() -> new UserCommentPageRequest(1, 51).toPageable())
                .isInstanceOf(InvalidPageRequestException.class);
    }
}
