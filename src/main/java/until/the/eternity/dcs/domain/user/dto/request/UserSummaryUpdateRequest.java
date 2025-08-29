package until.the.eternity.dcs.domain.user.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSummaryUpdateRequest(
        @Schema(description = "사용자 ID", example = "1L", requiredMode = REQUIRED)
                @NotBlank(message = "사용자 ID값은 공란일 수 없습니다.")
                Long userId,
        @Schema(description = "닉네임", example = "bsko")
                @NotBlank(message = "닉네임은 필수 입니다.")
                @Size(max = 50, message = "닉네임은 50자를 초과할 수 없습니다.")
                String nickname,
        @Schema(description = "레벨", example = "1") Integer level,
        @Schema(description = "사용자 등급", example = "USER") String grade) {}
