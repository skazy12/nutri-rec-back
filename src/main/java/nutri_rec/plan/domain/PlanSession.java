package nutri_rec.plan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plan_session")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlanSession {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "dias_plan", nullable = false)
    private int diasPlan;

    @Column(name = "top_n_recetas", nullable = false)
    private int topNRecetas;

    // snapshot del momento de generar plan (JSON string)
    @Column(name = "restricciones", columnDefinition = "text")
    private String restriccionesJson;

    // snapshot excluir_ids del momento de generar plan (JSON string)
    @Column(name = "excluir_ids", columnDefinition = "text")
    private String excluirIdsJson;

    @Column(name = "payload_mode", columnDefinition = "text")
    private String payloadMode;

    // guardas la respuesta del modelo (o parte) para auditoría
    @Column(name = "respuesta_mo", columnDefinition = "text")
    private String respuestaMo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }
}
