package nutri_rec.plan.infrastructure;

import nutri_rec.plan.domain.PlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanItemRepository extends JpaRepository<PlanItem, UUID> {
    List<PlanItem> findByPlanSessionIdOrderByDiaAscOrdenAsc(UUID planSessionId);
    Optional<PlanItem> findById(UUID id);
}
