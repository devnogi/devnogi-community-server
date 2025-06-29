package until.the.eternity.dcs.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "게시글 검색 요청 DTO")
public class PostSearchRequest {

    @Schema(description = "게시판 ID", example = "1")
    private Long boardId;

    @Schema(description = "검색 키워드", example = "Spring Boot")
    private String keyword;

    @Schema(description = "검색 타입", example = "TITLE_CONTENT", allowableValues = {"TITLE", "CONTENT", "TITLE_CONTENT", "AUTHOR"})
    private String searchType; // TITLE, CONTENT, TITLE_CONTENT, AUTHOR

    @Schema(description = "태그 목록", example = "[\"Spring\", \"JPA\"]")
    private List<String> tags;

    @Schema(description = "임시저장 여부", example = "false")
    private Boolean isDraft;

    @Schema(description = "차단 여부", example = "false")
    private Boolean isBlocked;

    @Schema(description = "시작 날짜", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "종료 날짜", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0", minimum = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20", minimum = "1", maximum = "100")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "정렬 기준", example = "createdAt", defaultValue = "createdAt", allowableValues = {"createdAt", "updatedAt", "viewCount", "likeCount", "commentCount", "title"})
    @Builder.Default
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향", example = "DESC", defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
    @Builder.Default
    private String sortDirection = "DESC";

    public Pageable toPageable() {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(page, size, sort);
    }
}