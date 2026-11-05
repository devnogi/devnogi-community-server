package until.the.eternity.dcs.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import until.the.eternity.dcs.common.exception.InvalidPageRequestException;

public record UserCommentPageRequest(
        @Schema(description = "요청할 페이지 번호", example = "1") Integer page,
        @Schema(description = "페이지당 항목 수", example = "20") Integer size) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    public Pageable toPageable() {
        int resolvedPage = page != null ? page : DEFAULT_PAGE;
        int resolvedSize = size != null ? size : DEFAULT_SIZE;
        if (resolvedPage < 1 || resolvedSize < 1 || resolvedSize > MAX_SIZE) {
            throw new InvalidPageRequestException();
        }

        return PageRequest.of(
                resolvedPage - 1,
                resolvedSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }
}
