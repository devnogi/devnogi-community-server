package until.the.eternity.dcs.common.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import until.the.eternity.dcs.common.exception.InvalidPageRequestException;

class CustomPageRequestTest {

    @Test
    @DisplayName("기본값은 page=1, size=20을 사용한다.")
    void toPageable_WithDefaults() {
        // given
        CustomPageRequest request = new CustomPageRequest(null, null, null, null);

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("page가 1보다 작으면 예외를 던진다.")
    void toPageable_InvalidPage() {
        // given
        CustomPageRequest request = new CustomPageRequest(0, 10, "id", "desc");

        // when & then
        assertThatThrownBy(request::toPageable).isInstanceOf(InvalidPageRequestException.class);
    }

    @Test
    @DisplayName("size가 1보다 작으면 예외를 던진다.")
    void toPageable_InvalidSizeMin() {
        // given
        CustomPageRequest request = new CustomPageRequest(1, 0, "id", "desc");

        // when & then
        assertThatThrownBy(request::toPageable).isInstanceOf(InvalidPageRequestException.class);
    }

    @Test
    @DisplayName("size가 커도 공통 요청에서는 허용한다.")
    void toPageable_LargeSizeIsAllowed() {
        // given
        CustomPageRequest request = new CustomPageRequest(1, 51100, "id", "desc");

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getPageSize()).isEqualTo(51100);
    }

    @Test
    @DisplayName("sortBy가 빈 문자열이면 기본 정렬 필드(id)를 사용한다.")
    void toPageable_BlankSortBy_UsesDefault() {
        // given
        CustomPageRequest request = new CustomPageRequest(1, 20, "", "desc");

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    @DisplayName("sortBy가 공백 문자열이면 기본 정렬 필드(id)를 사용한다.")
    void toPageable_WhitespaceSortBy_UsesDefault() {
        // given
        CustomPageRequest request = new CustomPageRequest(1, 20, "   ", "desc");

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getSort().getOrderFor("id")).isNotNull();
    }
}
