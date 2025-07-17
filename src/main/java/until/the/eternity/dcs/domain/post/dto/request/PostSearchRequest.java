package until.the.eternity.dcs.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostSearchRequest(
        @Schema(description = "검색 키워드", example = "길드") String keyword,
        @Schema(description = "게시판 ID", example = "1") Long boardId,
        @Schema(description = "작성자 ID", example = "1") Long userId,
        @Schema(description = "태그", example = "길드") String tag,
        @Schema(description = "임시저장 여부", example = "false") Boolean isDraft

        //        @Schema(description = "시작일 (yyyy-MM-dd)")  TODO: 기간을 조건으로 검색하기를 추가하는게 좋을까요?
        //        String startDate,
        //
        //        @Schema(description = "종료일 (yyyy-MM-dd)")
        //        String endDate

        ) {}
