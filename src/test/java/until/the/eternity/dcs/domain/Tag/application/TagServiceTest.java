package until.the.eternity.dcs.domain.Tag.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.tag.application.TagService;
import until.the.eternity.dcs.domain.tag.dto.response.TagResponse;
import until.the.eternity.dcs.domain.tag.entity.PostTag;
import until.the.eternity.dcs.domain.tag.entity.Tag;
import until.the.eternity.dcs.domain.tag.infrastructure.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagServiceTest 단위 테스트")
public class TagServiceTest {
    @Mock private TagRepository tagRepository;

    @InjectMocks private TagService tagService;

    @Test
    @DisplayName("태그 생성 성공")
    public void createTag_Success() {
        // given

        String tagName = "test";
        Tag tag = Tag.builder().name(tagName).build();
        when(tagRepository.save(Mockito.any(Tag.class))).thenReturn(tag);

        // when

        Tag newTag = tagService.createTag(tagName);

        // then

        assertThat(newTag.getName()).isEqualTo(tagName);
    }

    @Test
    @DisplayName("태그 리스트 조회 성공")
    public void findTag_success() {

        // given
        Long postId = 1L;
        Post post = Post.builder().id(postId).postTags(new ArrayList<>()).build();

        Tag tag1 = Tag.builder().id(1L).name("java").build();
        Tag tag2 = Tag.builder().id(2L).name("spring").build();

        PostTag postTag1 = PostTag.builder().id(1L).post(post).tag(tag1).build();
        PostTag postTag2 = PostTag.builder().id(2L).post(post).tag(tag2).build();
        post.getPostTags().add(postTag1);
        post.getPostTags().add(postTag2);

        List<Tag> tagList = new ArrayList<>();
        tagList.add(tag1);
        tagList.add(tag2);

        when(tagRepository.findAllByPostId(postId)).thenReturn(tagList);

        // When

        List<TagResponse> tagResponses = tagService.findByPostId(postId);

        // Then
        assertThat(tagResponses).isNotNull();
        assertThat(tagResponses).hasSize(2);
        assertThat(tagResponses.stream().map(TagResponse::name).collect(Collectors.toList()))
                .containsExactlyInAnyOrder("java", "spring");
        verify(tagRepository).findAllByPostId(postId);
    }
}
