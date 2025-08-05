package until.the.eternity.dcs.domain.announcement.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.announcement.infrastructure.JpaAnnouncementRepository;

@Repository
@RequiredArgsConstructor
public class AnnouncementRepository {
    private final JpaAnnouncementRepository jpaRepository;
}
