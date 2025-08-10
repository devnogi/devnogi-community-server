package until.the.eternity.dcs.domain.tag.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.dto.response.TagResponse;
import until.the.eternity.dcs.domain.tag.entity.Tag;
import until.the.eternity.dcs.domain.tag.infrastructure.TagRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    public Tag findOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName).orElseGet(() -> createTag(tagName));
    }

    @Transactional
    public Tag createTag(String tagName) {
        return tagRepository.save(Tag.builder().name(tagName).build());
    }

    public List<TagResponse> findByPostId(Long postId) {
        Post post = getPost(postId);

        return post.getPostTags().stream()
                .map(postTag -> new TagResponse(postTag.getTag().getName()))
                .toList();
    }

    public Post getPost(Long postId) {
        return postRepository
                .findWithTagsById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }
}
