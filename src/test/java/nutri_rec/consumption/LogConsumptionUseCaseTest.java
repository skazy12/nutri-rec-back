package nutri_rec.consumption;

import nutri_rec.consumption.application.LogConsumptionUseCase;
import nutri_rec.consumption.domain.ConsumptionLog;
import nutri_rec.consumption.infrastructure.ConsumptionLogRepository;
import nutri_rec.plan.domain.PlanItem;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LogConsumptionUseCaseTest {

    @Test
    void test_execute_upsert_creates_new_log_if_not_exists() {
        System.out.println("[UT-BE-13] LogConsumptionUseCase upsert: crea si no existe");

        ConsumptionLogRepository logRepo = mock(ConsumptionLogRepository.class);
        PlanItemRepository planItemRepo = mock(PlanItemRepository.class);

        LogConsumptionUseCase uc = new LogConsumptionUseCase(logRepo, planItemRepo);

        UUID userId = UUID.randomUUID();
        UUID planItemId = UUID.randomUUID();

        PlanItem planItem = PlanItem.builder()
                .id(planItemId)
                .recipeId(777)
                .tipoComida("Almuerzo")
                .dia(1)
                .orden(2)
                .planSessionId(UUID.randomUUID())
                .build();

        when(planItemRepo.findById(planItemId)).thenReturn(Optional.of(planItem));
        when(logRepo.findByUserIdAndPlanItemIdAndFecha(userId, planItemId, LocalDate.of(2026, 1, 5)))
                .thenReturn(Optional.empty());

        when(logRepo.save(any(ConsumptionLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsumptionLog out = uc.execute(
                userId,
                planItemId,
                LocalDate.of(2026, 1, 5),
                true,
                BigDecimal.valueOf(1.5),
                "ok"
        );

        System.out.println("  Entradas: userId=" + userId + " planItemId=" + planItemId + " fecha=2026-01-05");
        System.out.println("  Obtenido:");
        System.out.println("    consumido=" + out.getConsumido());
        System.out.println("    porciones=" + out.getCantidadPorciones());
        System.out.println("    recipeId=" + out.getRecipeId());
        System.out.println("    recipeIdConsumida=" + out.getRecipeIdConsumida());
        System.out.println("  Esperado: recipeId y recipeIdConsumida == planItem.recipeId (777)");

        assertEquals(true, out.getConsumido());
        assertEquals(BigDecimal.valueOf(1.5), out.getCantidadPorciones());
        assertEquals(777, out.getRecipeId());
        assertEquals(777, out.getRecipeIdConsumida());
    }

    @Test
    void test_execute_upsert_updates_existing_log() {
        System.out.println("[UT-BE-14] LogConsumptionUseCase upsert: actualiza si existe (Opción B)");

        ConsumptionLogRepository logRepo = mock(ConsumptionLogRepository.class);
        PlanItemRepository planItemRepo = mock(PlanItemRepository.class);

        LogConsumptionUseCase uc = new LogConsumptionUseCase(logRepo, planItemRepo);

        UUID userId = UUID.randomUUID();
        UUID planItemId = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(2026, 1, 5);

        PlanItem planItem = PlanItem.builder()
                .id(planItemId)
                .recipeId(888)
                .tipoComida("Cena")
                .dia(1)
                .orden(3)
                .planSessionId(UUID.randomUUID())
                .build();

        ConsumptionLog existing = ConsumptionLog.builder()
                .userId(userId)
                .planItemId(planItemId)
                .fecha(fecha)
                .build();

        when(planItemRepo.findById(planItemId)).thenReturn(Optional.of(planItem));
        when(logRepo.findByUserIdAndPlanItemIdAndFecha(userId, planItemId, fecha))
                .thenReturn(Optional.of(existing));

        when(logRepo.save(any(ConsumptionLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsumptionLog out = uc.execute(
                userId, planItemId, fecha,
                false, BigDecimal.valueOf(2), "nota"
        );

        System.out.println("  Obtenido: consumido=" + out.getConsumido() + " porciones=" + out.getCantidadPorciones() + " recipeId=" + out.getRecipeId());
        System.out.println("  Esperado: actualiza campos y recipeId=888");

        assertEquals(false, out.getConsumido());
        assertEquals(BigDecimal.valueOf(2), out.getCantidadPorciones());
        assertEquals("nota", out.getNota());
        assertEquals(888, out.getRecipeId());
    }
}
