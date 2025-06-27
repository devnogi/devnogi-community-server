package until.the.eternity.dcs.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreRemove;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class SoftDeleteEntity extends AuditableEntity {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PreRemove
    protected void onDelete(){
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void delete(Long userId){
        onDelete();
        this.setUpdatedBy(userId);
    }
}