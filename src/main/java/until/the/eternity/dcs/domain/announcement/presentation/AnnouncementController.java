package until.the.eternity.dcs.domain.announcement.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.dcs.domain.announcement.application.AnnouncementService;
import until.the.eternity.dcs.domain.announcement.dto.request.AnnouncementCreateRequest;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementPersistResponse;
import until.the.eternity.dcs.domain.announcement.dto.response.AnnouncementToggleResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    // todo Swagger & valid
    @PostMapping("/{postId}")
    public ResponseEntity<AnnouncementPersistResponse> create(
            @PathVariable Long postId, @RequestBody AnnouncementCreateRequest announcement) {
        return ResponseEntity.status(CREATED)
                .body(announcementService.create(postId, announcement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PatchMapping("/toggle-global/{id}")
    public AnnouncementToggleResponse toggleGlobal(@PathVariable Long id) {
        return announcementService.toggleGlobal(id);
    }
}
