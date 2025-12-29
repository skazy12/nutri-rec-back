package nutri_rec.recommendation.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record RecommendationResult(
        JsonNode requerimientos,
        JsonNode recomendaciones,
        JsonNode plan
) {}