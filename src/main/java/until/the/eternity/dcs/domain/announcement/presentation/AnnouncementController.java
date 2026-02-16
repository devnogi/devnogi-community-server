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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            summary = "怨듭?湲 ?깅줉 API",
            description = """
			- Description : ??API??寃뚯떆湲??怨듭?湲濡??깅줉?⑸땲??
			- Assignee : ?댁떊??		""")
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
            summary = "怨듭?湲 ??젣 API",
            description = """
			- Description : ??API??怨듭?湲?먯꽌 ??젣?⑸땲??
			- Assignee : ?댁떊??		""")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PatchMapping("/toggle-global/{id}")
    @Operation(
            summary = "怨듭?湲 ?꾩껜 怨듦컻 ?щ? ?좉? API",
            description =
                    """
			- Description : ??API??怨듭?湲???꾩껜 怨듦컻 ?щ?瑜??좉??⑸땲??
			- Assignee : ?댁떊??		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = AnnouncementToggleResponse.class)))
    public AnnouncementToggleResponse toggleGlobal(@PathVariable Long id) {
        return announcementService.toggleGlobal(id);
    }

    @GetMapping
    @Operation(
            summary = "寃뚯떆??蹂?怨듭?湲 ?꾩껜 議고쉶 API",
            description = """
	- Description : boardId媛 null ?쇰븣 ?곗튂怨듦컻 怨듭?湲留??곌껐?쒕떎. boardId媛 ?놁쓣 寃쎌슦 해당 board怨듭?湲??곹깭?먮? ?꾩튂怨듦컻 怨듭?湲源뚯? 議고쉶?쒕떎.
	- Assignee : ?댁떊??""")
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
