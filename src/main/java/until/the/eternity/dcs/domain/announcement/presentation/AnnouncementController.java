package until.the.eternity.dcs.domain.announcement.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.dcs.domain.announcement.application.AnnouncementService;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPageResponseItem;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    @PostMapping("/{postId}")
    @Operation(
            summary = "공지글 등록 API",
            description = """
			- Description : 이 API는 게시글을 공지글로 등록합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "201",
            content =
                    @Content(schema = @Schema(implementation = AnnouncementPersistResponse.class)))
    public ResponseEntity<AnnouncementPersistResponse> create(
            @PathVariable Long postId, @Valid @RequestBody AnnouncementCreateRequest request) {
        return ResponseEntity.status(CREATED).body(announcementService.create(postId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "공지글 삭제 API",
            description = """
			- Description : 이 API는 공지글에서 삭제합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PatchMapping("/toggle-global/{id}")
    @Operation(
            summary = "공지글 전체 공개 여부 토글 API",
            description =
                    """
			- Description : 이 API는 공지글의 전체 공개 여부를 토글합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = AnnouncementToggleResponse.class)))
    public AnnouncementToggleResponse toggleGlobal(@PathVariable Long id) {
        return announcementService.toggleGlobal(id);
    }

    @GetMapping("/{boardId}")
    @Operation(
            summary = "게시판 별 공지글 전체 조회 API",
            description = """
	- Description : 이 API는 해당 게시판의 공지글을 전체 조회합니다.
	- Assignee : 이신행
""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(schema = @Schema(implementation = AnnouncementPageResponseItem.class)))
    public List<AnnouncementPageResponseItem> getAnnouncements(@PathVariable Long boardId) {
        return announcementService.getAnnouncementByBoardId(boardId);
    }

    @GetMapping
    @Operation(
            summary = "전역 공지글 전체 조회 API",
            description = """
	- Description : 이 API는 전체 공개 상태의 공지글만 조회합니다.
	- Assignee : 이신행
""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(schema = @Schema(implementation = AnnouncementPageResponseItem.class)))
    public List<AnnouncementPageResponseItem> getGlobalAnnouncements() {
        return announcementService.getGlobalAnnouncements();
    }
}
