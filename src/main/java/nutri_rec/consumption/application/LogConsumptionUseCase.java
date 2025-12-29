package nutri_rec.consumption.application;

import nutri_rec.consumption.domain.ConsumptionLog;
import nutri_rec.consumption.infrastructure.ConsumptionLogRepository;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LogConsumptionUseCase {

    private final ConsumptionLogRepository logRepo;
    private final PlanItemRepository planItemRepo;

    public LogConsumptionUseCase(ConsumptionLogRepository logRepo, PlanItemRepository planItemRepo) {
        this.logRepo = logRepo;
        this.planItemRepo = planItemRepo;
    }

    public ConsumptionLog execute(UUID userId,
                                  UUID planItemId,
                                  LocalDate fecha,
                                  Boolean consumido,
                                  BigDecimal porciones,
                                  String nota) {

        var planItem = planItemRepo.findById(planItemId)
                .orElseThrow(() -> new RuntimeException("Plan item no encontrado"));

        // Upsert por (userId, planItemId, fecha)
        var existing = logRepo.findByUserIdAndPlanItemIdAndFecha(userId, planItemId, fecha);

        ConsumptionLog log = existing.orElseGet(() -> ConsumptionLog.builder()
                .userId(userId)
                .planItemId(planItemId)
                .fecha(fecha)
                .createdAt(Instant.now())
                .build());

        log.setConsumido(consumido);
        log.setCantidadPorciones(porciones);
        log.setNota(nota);

        // “recipe_id” y “recipe_id_consumida” se fijan con la receta vigente del plan_item al registrar
        log.setRecipeId(planItem.getRecipeId());
        log.setRecipeIdConsumida(planItem.getRecipeId());

        return logRepo.save(log);
    }
}
