package until.the.eternity.dcs.domain.announcement.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.dcs.domain.announcement.application.AnnouncementService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcement")
public class AnnouncementController {
    private final AnnouncementService announcementService;
}
