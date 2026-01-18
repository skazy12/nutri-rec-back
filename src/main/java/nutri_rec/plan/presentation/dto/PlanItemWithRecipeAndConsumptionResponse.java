package nutri_rec.plan.presentation.dto;

import nutri_rec.consumption.domain.ConsumptionLog;
import nutri_rec.plan.domain.PlanItem;
import nutri_rec.recipe.domain.Recipe;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PlanItemWithRecipeAndConsumptionResponse(
        UUID planItemId,
        Integer dia,
        Integer orden,
        String tipoComida,

        LocalDate fechaPlanificada,

        Integer recipeId,
        String recipeNombre,
        String recipeImagenUrl,

        Boolean consumido,
        BigDecimal cantidadPorciones,
        String nota
) {
    /**
     * Arma el item del Home combinando:
     * - PlanItem (qué toca comer)
     * - Recipe (nombre/imagen)
     * - ConsumptionLog (si consumió o no en la fecha planificada)
     */
    public static PlanItemWithRecipeAndConsumptionResponse from(
            PlanItem planItem,
            Recipe recipe,
            LocalDate fechaPlanificada,
            ConsumptionLog log
    ) {
        boolean consumed = log != null && Boolean.TRUE.equals(log.getConsumido());

        return new PlanItemWithRecipeAndConsumptionResponse(
                planItem.getId(),
                planItem.getDia(),
                planItem.getOrden(),
                planItem.getTipoComida(),
                fechaPlanificada,
                planItem.getRecipeId(),
                recipe != null ? recipe.getNombre() : null,
                recipe != null ? recipe.getImagen_url() : null,
                consumed,
                log != null ? log.getCantidadPorciones() : null,
                log != null ? log.getNota() : null
        );
    }
}
