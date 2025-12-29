package nutri_rec.consumption.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "consumption_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_consumption_user_item_fecha",
                columnNames = {"user_id", "plan_item_id", "fecha"}
        ))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumptionLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "recipe_id", nullable = false)
    private int recipeId;

    @Column(name = "plan_item_id")
    private UUID planItemId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "consumido")
    private Boolean consumido;

    @Column(name = "cantidad_porciones", precision = 6, scale = 2)
    private BigDecimal cantidadPorciones;

    @Column(name = "nota", columnDefinition = "text")
    private String nota;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // receta “real” al momento del registro (para trazabilidad si luego haces swap)
    @Column(name = "recipe_id_consumida")
    private Integer recipeIdConsumida;
}
