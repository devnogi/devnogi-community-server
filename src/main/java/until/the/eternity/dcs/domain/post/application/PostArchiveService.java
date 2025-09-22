package until.the.eternity.dcs.domain.post.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.post.infrastructure.PostArchiveRepository;

@Service
@RequiredArgsConstructor
public class PostArchiveService {
    private final PostArchiveRepository postArchiveRepository;
}
