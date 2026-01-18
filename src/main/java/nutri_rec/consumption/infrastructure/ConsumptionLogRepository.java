package nutri_rec.consumption.infrastructure;

import nutri_rec.consumption.domain.ConsumptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsumptionLogRepository extends JpaRepository<ConsumptionLog, UUID> {
    Optional<ConsumptionLog> findByUserIdAndPlanItemIdAndFecha(UUID userId, UUID planItemId, LocalDate fecha);

    boolean existsByUserIdAndPlanItemIdAndConsumidoIsTrue(UUID userId, UUID planItemId);

    // ✅ NUEVO: delete por (userId, planItemId, fecha)
    @Modifying
    @Transactional
    long deleteByUserIdAndPlanItemIdAndFecha(UUID userId, UUID planItemId, LocalDate fecha);

    List<ConsumptionLog> findByUserIdAndPlanItemIdInAndFecha(UUID userId, List<UUID> planItemIds, LocalDate fecha);

    List<ConsumptionLog> findByUserIdAndPlanItemIdInAndFechaBetween(
            UUID userId,
            List<UUID> planItemIds,
            LocalDate from,
            LocalDate to
    );
    boolean existsByUserIdAndPlanItemIdAndFechaAndConsumidoIsTrue(
            UUID userId, UUID planItemId, LocalDate fecha
    );


}
