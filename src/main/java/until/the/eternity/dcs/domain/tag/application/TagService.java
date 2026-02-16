package until.the.eternity.dcs.domain.tag.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.tag.dto.response.TagResponse;
import until.the.eternity.dcs.domain.tag.entity.Tag;
import until.the.eternity.dcs.domain.tag.infrastructure.TagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tagRepository;

    public Tag findOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName).orElseGet(() -> createTag(tagName));
    }

    @Transactional
    public Tag createTag(String tagName) {
        return tagRepository.save(Tag.builder().name(tagName).build());
    }

    public List<TagResponse> findByPostId(Long postId) {
        return tagRepository.findAllByPostId(postId).stream()
                .map(tag -> TagResponse.builder().name(tag.getName()).build())
                .toList();
    }
}
