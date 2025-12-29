package nutri_rec.plan.presentation.dto;

import nutri_rec.plan.domain.PlanItem;

import java.util.UUID;

public record PlanItemResponse(
        UUID planItemId,
        Integer recipeId,
        Integer dia,
        String tipoComida,
        Integer orden
) {
    public static PlanItemResponse from(PlanItem pi) {
        return new PlanItemResponse(
                pi.getId(),
                pi.getRecipeId(),
                pi.getDia(),
                pi.getTipoComida(),
                pi.getOrden()
        );
    }
}
