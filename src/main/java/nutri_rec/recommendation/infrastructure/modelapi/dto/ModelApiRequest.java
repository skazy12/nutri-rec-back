package nutri_rec.recommendation.infrastructure.modelapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ModelApiRequest(
        String sexo,
        int edad,
        double peso,
        double talla,
        String nivel_actividad,
        int comidas_diarias,
        String tipo_dieta,
        @JsonProperty("restricciones")
        List<String> restricciones,

        String objetivo_nutricional,
        Integer dias_plan,
        Integer top_n_recetas,
        @JsonProperty("almuerzoCenaMisma")
        Boolean almuerzoCenaMisma,
        @JsonProperty("excluir_ids")
        List<Integer> excluirIds
) {}
