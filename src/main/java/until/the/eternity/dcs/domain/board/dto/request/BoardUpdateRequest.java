package until.the.eternity.dcs.domain.board.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardUpdateRequest(
        @Schema(description = "게시판 이름", example = "길드원 모집", requiredMode = REQUIRED)
                @NotBlank(message = "게시판의 이름은 공란일 수 없습니다.")
                @Size(max = 20, message = "게시판의 이름의 길이는 20자 보다 길 수 없습니다.")
                String name,
        @Schema(
                        description = "게시판 설명",
                        example = "길드나 길드원을 모집하는 게시판입니다.",
                        requiredMode = NOT_REQUIRED)
                String description,
        @Schema(description = "상위 카테고리", example = "자유 게시판", requiredMode = NOT_REQUIRED)
                @Size(max = 20, message = "게시판의 상위 카테고리의 길이는 20자 보다 길 수 없습니다.")
                String topCategory,
        @Schema(description = "하위 카테고리", example = "길드 게시판", requiredMode = NOT_REQUIRED)
                @Size(max = 20, message = "게시판의 하위 카테고리의 길이는 20자 보다 길 수 없습니다.")
                String subCategory) {}
