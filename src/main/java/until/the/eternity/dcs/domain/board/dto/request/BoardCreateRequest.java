package until.the.eternity.dcs.domain.board.dto.request;

// todo 카테고리를 ENUM으로 뺄까요?
//  Swagger용 어노테이션 (Schema)를 붙일까요?
//  validation용 어노테이션을 붙일까요?
public record BoardCreateRequest(
	String name,
	String description,
	String topCategory,
	String subCategory
) {
}