package nutri_rec.plan.presentation.dto;

import nutri_rec.plan.domain.PlanItem;
import nutri_rec.recipe.domain.Recipe;

import java.util.UUID;

public record PlanItemWithRecipeResponse(
        UUID planItemId,
        Integer recipeId,
        String recipeNombre,
        String imagenUrl,
        Integer dia,
        String tipoComida,
        Integer orden
) {
    public static PlanItemWithRecipeResponse from(PlanItem pi, Recipe r) {
        return new PlanItemWithRecipeResponse(
                pi.getId(),
                pi.getRecipeId(),
                r == null ? null : r.getNombre(),
                r == null ? null : r.getImagen_url(), // <- ver nota #2 si tu entidad aún no lo tiene
                pi.getDia(),
                pi.getTipoComida(),
                pi.getOrden()
        );
    }
}
