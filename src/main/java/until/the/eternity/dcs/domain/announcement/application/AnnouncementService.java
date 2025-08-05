package until.the.eternity.dcs.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.announcement.entity.AnnouncementRepository;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository repository;
}
