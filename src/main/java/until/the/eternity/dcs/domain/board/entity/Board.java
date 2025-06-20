package until.the.eternity.dcs.domain.board.entity;

import jakarta.persistence.*;
import lombok.*;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
import until.the.eternity.dcs.common.entity.SoftDeleteEntity;
import until.the.eternity.dcs.domain.post.entity.Post;

import java.util.List;

@Entity
@Table(name = "board")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "top_category", length = 50)
    private String topCategory;

    @Column(name = "sub_category", length = 50)
    private String subCategory;

    @Column(name = "created_by")
    private Long createdBy;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Announcement> announcements;
}