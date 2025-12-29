package nutri_rec.plan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plan_item_swap")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlanItemSwap {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "plan_item_id", nullable = false)
    private UUID planItemId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "old_recipe_id", nullable = false)
    private int oldRecipeId;

    @Column(name = "new_recipe_id", nullable = false)
    private int newRecipeId;

    @Column(name = "motivo", columnDefinition = "text")
    private String motivo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }
}
