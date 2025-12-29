package nutri_rec.consumption.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LogConsumptionRequest(
        UUID planItemId,
        LocalDate fecha,
        Boolean consumido,
        BigDecimal cantidadPorciones,
        String nota
) {}
