package until.the.eternity.dcs.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "originalFileName", nullable = false, columnDefinition = "사용자가 저장할 때 지정된 파일명")
    private String originalFileName;

    @Column(name = "storedFileName", nullable = false, columnDefinition = "UUID를 통해 새롭게 지정된 파일명")
    private String storedFileName;
}
