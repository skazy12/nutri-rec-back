package nutri_rec.plan.infrastructure;

import nutri_rec.plan.domain.PlanItemSwap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanItemSwapRepository extends JpaRepository<PlanItemSwap, UUID> {
    List<PlanItemSwap> findByPlanItemIdOrderByCreatedAtDesc(UUID planItemId);
}
