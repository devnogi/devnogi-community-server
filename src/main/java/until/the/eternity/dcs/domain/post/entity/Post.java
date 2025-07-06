package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.common.entity.SoftDeleteEntity;
import until.the.eternity.dcs.domain.postLike.entity.PostLike;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

import java.util.List;


@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "is_draft", nullable = false)
    @Builder.Default
    private Boolean isDraft = false;

    @Column(name = "is_blocked")
    @Builder.Default
    private Boolean isBlocked = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PostLike> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PostTag> postTags;

    public void update(String title, String content, Boolean isDraft, List<PostTag> postTags, Long userId){
        if(title != null){
            this.title = title;
        }
        if(content != null){
            this.content = content;
        }
        if(isDraft != null){
            this.isDraft = isDraft;
        }
        this.postTags = postTags;
        this.setUpdatedBy(userId);
    }
}