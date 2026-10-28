package until.the.eternity.dcs.domain.board.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.board.entity.Board;

@Builder
public record BoardDetailResponse(
        @Schema(description = "게시판 아이디", example = "1", requiredMode = REQUIRED) Long id,
        @Schema(description = "게시판 이름", example = "길드원 모집", requiredMode = REQUIRED) String name,
        @Schema(
                        description = "게시판 설명",
                        example = "길드나 길드원을 모집하는 게시판입니다.",
                        requiredMode = NOT_REQUIRED)
                String description,
        @Schema(description = "상위 카테고리", example = "자유 게시판", requiredMode = NOT_REQUIRED)
                String topCategory,
        @Schema(description = "하위 카테고리", example = "길드 게시판", requiredMode = NOT_REQUIRED)
                String subCategory) {
    public static BoardDetailResponse from(Board board) {
        return BoardDetailResponse.builder()
                .id(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .topCategory(board.getTopCategory())
                .subCategory(board.getSubCategory())
                .build();
    }
}
