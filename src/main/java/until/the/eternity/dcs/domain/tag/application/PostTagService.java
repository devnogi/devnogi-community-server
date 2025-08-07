package until.the.eternity.dcs.domain.tag.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.tag.entity.PostTag;
import until.the.eternity.dcs.domain.tag.infrastructure.PostTagRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostTagService {
    private final PostTagRepository postTagRepository;

    @Transactional
    public void savePostTags(List<PostTag> postTags) {
        postTagRepository.saveAll(postTags);
    }
}
