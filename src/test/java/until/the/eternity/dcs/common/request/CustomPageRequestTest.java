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
    @DisplayName("size가 10보다 작으면 예외를 던진다.")
    void toPageable_InvalidSizeMin() {
        // given
        CustomPageRequest request = new CustomPageRequest(1, 9, "id", "desc");

        // when & then
        assertThatThrownBy(request::toPageable).isInstanceOf(InvalidPageRequestException.class);
    }

    @Test
    @DisplayName("size가 50보다 크면 예외를 던진다.")
    void toPageable_InvalidSizeMax() {
        // given
        CustomPageRequest request = new CustomPageRequest(1, 51, "id", "desc");

        // when & then
        assertThatThrownBy(request::toPageable).isInstanceOf(InvalidPageRequestException.class);
    }
}
