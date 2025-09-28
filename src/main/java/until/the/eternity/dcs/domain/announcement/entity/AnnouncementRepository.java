package until.the.eternity.dcs.domain.announcement.entity;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

// @Repository
@RequiredArgsConstructor
public class AnnouncementRepository {
    private final until.the.eternity.dcs.domain.announcement.infrastructure.AnnouncementRepository
            jpaRepository;

    public Announcement save(Announcement announcement) {
        return jpaRepository.save(announcement);
    }

    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    public boolean existsByPostId(Long postId) {
        return jpaRepository.existsByPostId(postId);
    }

    public Optional<Announcement> findById(Long id) {
        return jpaRepository.findById(id);
    }

    public List<Announcement> findByBoardIdAndGlobal(Long boardId) {
        return jpaRepository.findByBoardIdOrIsGlobalTrue(boardId);
    }
}
