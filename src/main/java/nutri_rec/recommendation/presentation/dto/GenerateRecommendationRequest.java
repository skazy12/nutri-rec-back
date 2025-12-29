package nutri_rec.recommendation.presentation.dto;

import java.util.List;

public record GenerateRecommendationRequest(
        String sexo,
        int edad,
        double peso,
        double talla,
        String nivel_actividad,
        int comidas_diarias,
        String tipo_dieta,
        List<String> restricciones,
        String objetivo_nutricional,
        Integer dias_plan,
        Integer top_n_recetas,
        List<Integer> excluir_ids,
        List<String> excluir_nombres
) {}
