package until.the.eternity.dcs.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record PostSearchRequest(
        @Schema(description = "검색 키워드")
        String keyword,

        @Schema(description = "게시판 ID")
        Long boardId,

        @Schema(description = "작성자 ID")
        Long userId,

        @Schema(description = "태그")
        String tag,

        @Schema(description = "임시저장 여부")
        Boolean isDraft,

        @Schema(description = "시작일 (yyyy-MM-dd)")
        String startDate,

        @Schema(description = "종료일 (yyyy-MM-dd)")
        String endDate,

        @Schema(description = "정렬 기준 (created_at, view_count, like_count)")
        String sortBy,

        @Schema(description = "정렬 방향 (asc, desc)")
        String sortDirection,

        @Schema(description = "페이지 번호 (1부터 시작)", example = "0")
        @Min(value = 0, message = "페이지 번호는 1 이상이어야 합니다.")
        Integer page,

        @Schema(description = "페이지 크기", example = "20")
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
        Integer size
) {

}