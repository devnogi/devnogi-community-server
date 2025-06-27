package until.the.eternity.dcs.domain.board.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import until.the.eternity.dcs.common.entity.SoftDeleteEntity;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;
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

    public void update(String name, String description, String topCategory, String subCategory, Long updatedBy) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (topCategory != null) {
            this.topCategory = topCategory;
        }
        if (subCategory != null) {
            this.subCategory = subCategory;
        }
        this.setUpdatedBy(updatedBy);
    }
}