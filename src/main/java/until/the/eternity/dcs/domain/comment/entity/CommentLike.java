package until.the.eternity.dcs.domain.comment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "comment_like")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLike {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "comment_id", nullable = false)
	private Long commentId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	// BaseEntity의 updatedAt은 필요 없기 때문에 상속받지 않고 createdAt만 따로 추가
	@Setter
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

}