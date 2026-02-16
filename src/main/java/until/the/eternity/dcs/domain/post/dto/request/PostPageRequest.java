package until.the.eternity.dcs.domain.post.dto.request;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import until.the.eternity.dcs.common.exception.InvalidPageRequestException;

public record PostPageRequest(
        @Schema(description = "요청할 페이지 번호", example = "1") Integer page,
        @Schema(description = "페이지당 항목 수", example = "20") Integer size,
        @Schema(description = "정렬 필드 (예: createdAt)", example = "createdAt") String sortBy,
        @Schema(description = "정렬 방향 (asc or desc)", example = "desc") String direction) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "id", "likeCount", "viewCount");
    private static final int MIN_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public Pageable toPageable() {
        int resolvedPage = this.page != null ? this.page : DEFAULT_PAGE;
        int resolvedSize = this.size != null ? this.size : DEFAULT_SIZE;
        String resolvedSortBy = resolveSortBy(this.sortBy);
        Direction resolvedDirection = parseDirection(this.direction);
        validateRange(resolvedPage, resolvedSize);

        return PageRequest.of(
                resolvedPage - 1, resolvedSize, Sort.by(resolvedDirection, resolvedSortBy));
    }

    private void validateRange(int page, int size) {
        if (page < 1 || size < MIN_SIZE || size > MAX_SIZE) {
            throw new InvalidPageRequestException();
        }
    }

    private Direction parseDirection(String dir) {
        if (dir != null && dir.equalsIgnoreCase("asc")) {
            return ASC;
        }
        return DESC;
    }

    private String resolveSortBy(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_SORT_BY;
        }
        if (!ALLOWED_SORT_FIELDS.contains(value)) {
            throw new InvalidPageRequestException();
        }
        return value;
    }
}
