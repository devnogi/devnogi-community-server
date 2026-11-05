package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import until.the.eternity.dcs.domain.post.enums.PostMediaType;

@Entity
@Table(name = "post_image")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "original_file_name", nullable = false)
    @Comment("사용자가 저장할 때 지정된 파일명")
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    @Comment("UUID를 통해 새롭게 지정된 파일명")
    private String storedFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 16)
    @Builder.Default
    private PostMediaType mediaType = PostMediaType.IMAGE;

    @Column(name = "mime_type", nullable = false, length = 100)
    @Builder.Default
    private String mimeType = "image/jpeg";

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public void changeDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
