package until.the.eternity.dcs.domain.post.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import until.the.eternity.dcs.common.exception.InvalidPageRequestException;

class PostPageRequestTest {

    @Test
    @DisplayName("기본값은 page=1, size=20, sortBy=createdAt, direction=desc를 사용한다.")
    void toPageable_WithDefaults() {
        // given
        PostPageRequest request = new PostPageRequest(null, null, null, null);

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
    }

    @Test
    @DisplayName("page가 1보다 작으면 예외를 던진다.")
    void toPageable_InvalidPage() {
        // given
        PostPageRequest request = new PostPageRequest(0, 10, "createdAt", "desc");

        // when & then
        assertThatThrownBy(request::toPageable).isInstanceOf(InvalidPageRequestException.class);
    }

    @Test
    @DisplayName("size가 10보다 작으면 예외를 던진다.")
    void toPageable_InvalidSizeMin() {
        // given
        PostPageRequest request = new PostPageRequest(1, 9, "createdAt", "desc");

        // when & then
        assertThatThrownBy(request::toPageable).isInstanceOf(InvalidPageRequestException.class);
    }

    @Test
    @DisplayName("size가 50보다 크면 예외를 던진다.")
    void toPageable_InvalidSizeMax() {
        // given
        PostPageRequest request = new PostPageRequest(1, 51, "createdAt", "desc");

        // when & then
        assertThatThrownBy(request::toPageable).isInstanceOf(InvalidPageRequestException.class);
    }

    @Test
    @DisplayName("sortBy가 빈 문자열이면 기본 정렬 필드(createdAt)를 사용한다.")
    void toPageable_BlankSortBy_UsesDefault() {
        // given
        PostPageRequest request = new PostPageRequest(1, 20, "", "desc");

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    @DisplayName("sortBy가 공백 문자열이면 기본 정렬 필드(createdAt)를 사용한다.")
    void toPageable_WhitespaceSortBy_UsesDefault() {
        // given
        PostPageRequest request = new PostPageRequest(1, 20, "   ", "desc");

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    @DisplayName("허용된 정렬 필드(likeCount, viewCount, id 등)는 그대로 적용된다.")
    void toPageable_AllowedSortBy() {
        // given
        PostPageRequest likeCountRequest = new PostPageRequest(1, 20, "likeCount", "desc");
        PostPageRequest viewCountRequest = new PostPageRequest(1, 20, "viewCount", "asc");
        PostPageRequest idRequest = new PostPageRequest(1, 20, "id", "desc");

        // when & then
        assertThat(likeCountRequest.toPageable().getSort().getOrderFor("likeCount")).isNotNull();
        assertThat(viewCountRequest.toPageable().getSort().getOrderFor("viewCount"))
                .isNotNull()
                .satisfies(order -> assertThat(order.isAscending()).isTrue());
        assertThat(idRequest.toPageable().getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    @DisplayName("허용되지 않은 정렬 필드는 InvalidPageRequestException을 발생시킨다.")
    void toPageable_InvalidSortBy() {
        // given
        PostPageRequest request = new PostPageRequest(1, 20, "title", "desc");

        // when & then
        assertThatThrownBy(request::toPageable).isInstanceOf(InvalidPageRequestException.class);
    }
}
