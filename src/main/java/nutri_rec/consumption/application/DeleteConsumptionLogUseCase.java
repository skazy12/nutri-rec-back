package nutri_rec.consumption.application;

import nutri_rec.consumption.infrastructure.ConsumptionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class DeleteConsumptionLogUseCase {

    private final ConsumptionLogRepository logRepo;

    public DeleteConsumptionLogUseCase(ConsumptionLogRepository logRepo) {
        this.logRepo = logRepo;
    }

    /**
     * Eliminación idempotente del consumo:
     * - Si existe registro (userId + planItemId + fecha) lo borra.
     * - Si no existe, no falla (estado final correcto: "no consumido").
     */
    @Transactional
    public long execute(UUID userId, UUID planItemId, LocalDate fecha) {
        if (planItemId == null || fecha == null) {
            throw new RuntimeException("planItemId y fecha son obligatorios");
        }

        return logRepo.deleteByUserIdAndPlanItemIdAndFecha(userId, planItemId, fecha);
    }
}
