package nutri_rec.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false, unique = true)
    private UUID userId;

    private String nombre;
    private String sexo;
    private Integer edad;
    @Column(precision = 6, scale = 2)
    private BigDecimal peso;

    @Column(precision = 5, scale = 2)
    private BigDecimal talla;
    private String nivelActividad;
    private Integer comidasDiarias;
    private String tipoDieta;
    private String objetivoNutricional;

    @Column(name= "calorias_diarias",precision = 10, scale = 2)
    private BigDecimal caloriasDiarias;

    @Column(name = "proteinas_g", precision = 10, scale = 2)
    private BigDecimal proteinasG;

    @Column(name = "carbohidratos_g", precision = 10, scale = 2)
    private BigDecimal carbohidratosG;

    @Column(name = "grasas_g", precision = 10, scale = 2)
    private BigDecimal grasasG;

    @Column(name = "calorias_por_comida", precision = 10, scale = 2)
    private BigDecimal caloriasPorComida;

    @Column(name = "proteinas_por_comida_g", precision = 10, scale = 2)
    private BigDecimal proteinasPorComidaG;

    @Column(name = "carbohidratos_por_comida_g", precision = 10, scale = 2)
    private BigDecimal carbohidratosPorComidaG;

    @Column(name = "grasas_por_comida_g", precision = 10, scale = 2)
    private BigDecimal grasasPorComidaG;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "restricciones", columnDefinition = "text")
    private String restriccionesJson;

    @Column(name = "excluir_ids", columnDefinition = "text")
    private String excluirIdsJson;

    @Column(name = "almuerzo_cena_misma", nullable = false)
    private Boolean almuerzoCenaMisma = false;



    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        var now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
