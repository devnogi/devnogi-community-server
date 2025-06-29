package until.the.eternity.dcs.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "페이지 응답 객체")
public record CustomPageResponse<T>(
	@Schema(description = "데이터 리스트")
	List<T> items,

	@Schema(description = "페이지 메타데이터")
	PageMeta meta
) {
	public static <T> CustomPageResponse<T> from(Page<T> page) {
		return new CustomPageResponse<>(page.getContent(), PageMeta.from(page));
	}
}
