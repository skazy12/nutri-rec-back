package nutri_rec.recommendation.infrastructure.modelapi.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record ModelApiResponse(
        JsonNode requerimientos,
        JsonNode recomendaciones,
        JsonNode plan
) {}
