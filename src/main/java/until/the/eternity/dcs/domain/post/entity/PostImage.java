package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

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
}
