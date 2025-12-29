package nutri_rec.plan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plan_item")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlanItem {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "plan_session_id", nullable = false)
    private UUID planSessionId;

    @Column(name = "recipe_id", nullable = false)
    private int recipeId;

    @Column(name = "dia", nullable = false)
    private int dia;

    // Guardamos el slot que arma el modelo: Desayuno/Almuerzo/Cena/Snack/Snack1/Snack2
    @Column(name = "tipo_comida", nullable = false, length = 30)
    private String tipoComida;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }
}
