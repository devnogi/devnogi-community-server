package until.the.eternity.dcs.domain.announcement.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.dcs.domain.announcement.application.AnnouncementService;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPageResponseItem;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    @PostMapping("/{postId}")
    @Operation(
            summary = "공지글 등록 API",
            description =
                    """
			- Description : 이 API를 통해 게시글을 공지글로 등록합니다.
			- Assignee : 담당자 미정""")
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
            description =
                    """
			- Description : 이 API를 통해 공지글을 삭제합니다.
			- Assignee : 담당자 미정""")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PatchMapping("/toggle-global/{id}")
    @Operation(
            summary = "공지글 전체 공지 토글 API",
            description =
                    """
			- Description : 공지글의 전체 공지 여부를 토글합니다.
			- Assignee : 담당자 미정""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = AnnouncementToggleResponse.class)))
    public AnnouncementToggleResponse toggleGlobal(@PathVariable Long id) {
        return announcementService.toggleGlobal(id);
    }

    @GetMapping
    @Operation(
            summary = "게시판별 공지글 조회 API",
            description =
                    """
		- Description : boardId가 없으면 전체 공지(isGlobal=true, isDraft=false)만 조회하고, boardId가 있으면 해당 게시판 공지(isDraft=false, isGlobal=true 또는 boardId 일치)와 전체 공지를 함께 조회합니다.
		- Assignee : 담당자 미정""")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(schema = @Schema(implementation = AnnouncementPageResponseItem.class)))
    public List<AnnouncementPageResponseItem> getAnnouncements(
            @RequestParam(value = "boardId", required = false) String boardId) {
        return announcementService.getAnnouncements(parseBoardId(boardId));
    }

    private Long parseBoardId(String boardId) {
        if (!StringUtils.hasText(boardId)) {
            return null;
        }
        return Long.valueOf(boardId);
    }
}
