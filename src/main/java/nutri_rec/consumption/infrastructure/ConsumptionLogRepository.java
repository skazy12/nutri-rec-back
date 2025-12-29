package nutri_rec.consumption.infrastructure;

import nutri_rec.consumption.domain.ConsumptionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ConsumptionLogRepository extends JpaRepository<ConsumptionLog, UUID> {
    Optional<ConsumptionLog> findByUserIdAndPlanItemIdAndFecha(UUID userId, UUID planItemId, LocalDate fecha);

    boolean existsByUserIdAndPlanItemIdAndConsumidoIsTrue(UUID userId, UUID planItemId);
}
