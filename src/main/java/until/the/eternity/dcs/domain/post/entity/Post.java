package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import until.the.eternity.dcs.common.entity.SoftDeleteEntity;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.post.enums.PostContentFormat;
import until.the.eternity.dcs.domain.post.enums.PostMediaType;
import until.the.eternity.dcs.domain.post.util.LegacyPlainTextConverter;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Post extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "plain_content", columnDefinition = "TEXT")
    private String plainContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_format", nullable = false, length = 32)
    @Builder.Default
    private PostContentFormat contentFormat = PostContentFormat.LEGACY_MARKDOWN;

    @Column(name = "is_draft", nullable = false)
    @Builder.Default
    private Boolean isDraft = false;

    @Column(name = "is_blocked")
    @Builder.Default
    private Boolean isBlocked = false;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = false)
    private List<Comment> comments;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private List<PostLike> likes;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private List<PostTag> postTags;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    public void update(
            String title, String content, Boolean isDraft, List<PostTag> postTags, Long userId) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (isDraft != null) {
            this.isDraft = isDraft;
        }
        if (postTags != null) {
            this.postTags.addAll(postTags);
        }

        if (userId != null) {
            this.setUpdatedBy(userId);
        }
    }

    public void updatePlainContent(String plainContent, Long userId) {
        this.plainContent = plainContent;
        this.content = plainContent;
        this.title = deriveTitle(plainContent);
        this.contentFormat = PostContentFormat.PLAIN_TEXT_V1;
        this.isDraft = false;
        this.setUpdatedBy(userId);
    }

    public String readableContent() {
        return plainContent != null
                ? plainContent
                : LegacyPlainTextConverter.convert(title, content);
    }

    public void addImage(String originalFileName, String storedFileName) {
        PostImage image =
                PostImage.builder()
                        .originalFileName(originalFileName)
                        .storedFileName(storedFileName)
                        .post(this)
                        .build();

        this.images.add(image);
    }

    public void addMedia(
            String originalFileName,
            String storedFileName,
            PostMediaType mediaType,
            String mimeType,
            long fileSize,
            int displayOrder) {
        images.add(
                PostImage.builder()
                        .originalFileName(originalFileName)
                        .storedFileName(storedFileName)
                        .mediaType(mediaType)
                        .mimeType(mimeType)
                        .fileSize(fileSize)
                        .displayOrder(displayOrder)
                        .post(this)
                        .build());
    }

    public void removeMedia(PostImage media) {
        images.remove(media);
    }

    private static String deriveTitle(String value) {
        String normalized = value == null ? "" : value.strip().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "게시글";
        }
        int end =
                normalized.offsetByCodePoints(
                        0, Math.min(60, normalized.codePointCount(0, normalized.length())));
        return normalized.substring(0, end);
    }
}
