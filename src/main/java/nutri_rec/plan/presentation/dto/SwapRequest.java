package nutri_rec.plan.presentation.dto;

import java.time.LocalDate;

public record SwapRequest(
        int newRecipeId,
        String motivo,
        LocalDate fecha
) {}
