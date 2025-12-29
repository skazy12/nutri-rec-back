package nutri_rec.recipe.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "recipe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    private Integer id;

    private String nombre;

    @Column(name = "tipo_comida")
    private String tipoComida;

    @Column(name = "categoria_plato")
    private String categoria_plato;

    /* 🔽 FALTABAN ESTOS 🔽 */

    @Column(columnDefinition = "text")
    private String ingredientes;

    @Column(columnDefinition = "text")
    private String preparacion;

    private Integer calorias;
    private BigDecimal proteinas;
    private BigDecimal grasas;
    private BigDecimal carbohidratos;

    private BigDecimal fibra;
    private BigDecimal azucares;
    private BigDecimal sodio;

    @Column(name = "region_plato")
    private String region_plato;

    @Column(name = "compatible_vegana")
    private String compatible_vegana;

    @Column(name = "compatible_vegetariana")
    private String compatible_vegetariana;

    @Column(name = "compatible_bajacarbo")
    private String compatible_bajacarbo;

    @Column(name = "contiene_lactosa")
    private String contiene_lactosa;

    @Column(name = "compatible_singluten")
    private String compatible_singluten;

    @Column(name = "sin_frutos_secos")
    private String sin_frutos_secos;

    @Column(name = "bajo_en_sodio")
    private String bajo_en_sodio;

    @Column(name = "alto_proteico")
    private String alto_proteico;

    @Column(name = "bajo_en_grasa")
    private String bajo_en_grasa;

    @Column(name = "alto_en_fibra")
    private String alto_en_fibra;

    @Column(name = "apto_diabetico")
    private String apto_diabetico;

    @Column(name = "fuente_url")
    private String fuente_url;

    @Column(name = "imagen_url")
    private String imagen_url;

    @Column(name = "created_at")
    private Instant createdAt;
}
