package nutri_rec.consumption.presentation.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item del checklist diario para Home.
 * Combina: PlanItem + Recipe + estado de consumo.
 */
public record DailyChecklistItemResponse(
        UUID planItemId,
        Integer diaPlan,
        Integer orden,
        String tipoComida,

        Integer recipeId,
        String recipeNombre,
        String recipeImagenUrl,

        Boolean consumido,
        BigDecimal cantidadPorciones,
        String nota
) {}
