package until.the.eternity.dcs.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@Schema(description = "페이지 응답 메타데이터")
public record PageMeta(
	@Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
	int currentPage,

	@Schema(description = "페이지당 항목 수", example = "20")
	int pageSize,

	@Schema(description = "전체 페이지 수", example = "5")
	int totalPages,

	@Schema(description = "전체 항목 수", example = "100")
	long totalElements,

	@Schema(description = "첫 페이지 여부", example = "true")
	boolean isFirst,

	@Schema(description = "마지막 페이지 여부", example = "false")
	boolean isLast
) {
	public static PageMeta from(Page<?> page) {
		return new PageMeta(
			page.getNumber() + 1,
			page.getSize(),
			page.getTotalPages(),
			page.getTotalElements(),
			page.isFirst(),
			page.isLast());
	}
}