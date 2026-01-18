package nutri_rec.consumption.presentation.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta del checklist diario para Home.
 */
public record DailyChecklistResponse(
        UUID planSessionId,
        LocalDate fecha,
        Integer diaPlan,
        List<DailyChecklistItemResponse> items
) {}
