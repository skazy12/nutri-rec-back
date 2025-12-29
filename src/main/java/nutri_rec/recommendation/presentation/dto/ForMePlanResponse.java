package nutri_rec.recommendation.presentation.dto;

import nutri_rec.plan.presentation.dto.PlanItemResponse;

import java.util.List;
import java.util.UUID;

public record ForMePlanResponse(
        UUID sessionId,
        List<PlanItemResponse> items,
        Object modeloPlan // aquí mandamos lo que ya devolvía el modelo (puede ser JsonNode)
) {}
