package until.the.eternity.dcs.domain.tag.entity;

import jakarta.persistence.*;
import lombok.*;
import until.the.eternity.dcs.domain.post.entity.Post;

@Entity
@Table(name = "post_tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
