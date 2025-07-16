package until.the.eternity.dcs.common.request;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public record CustomPageRequest(
        @Schema(description = "요청할 페이지 번호", example = "1") @Min(1) Integer page,
        @Schema(description = "페이지당 항목 수", example = "20") @Min(1) @Max(100) Integer size,
        @Schema(description = "정렬 필드 (예: createdAt)", example = "createdAt") String sortBy,
        @Schema(description = "정렬 방향 (asc or desc)", example = "desc") String direction) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";

    public Pageable toPageable() {
        int resolvedPage = this.page != null ? this.page : DEFAULT_PAGE;
        int resolvedSize = this.size != null ? this.size : DEFAULT_SIZE;
        String resolvedSortBy = this.sortBy != null ? this.sortBy : DEFAULT_SORT_BY;
        Direction resolvedDirection = parseDirection(this.direction);

        return PageRequest.of(
                resolvedPage - 1, resolvedSize, Sort.by(resolvedDirection, resolvedSortBy));
    }

    private Direction parseDirection(String dir) {
        if (dir != null && dir.equalsIgnoreCase("asc")) {
            return ASC;
        }
        return DESC;
    }
}
