package nutri_rec.plan.infrastructure;

import nutri_rec.plan.domain.PlanSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanSessionRepository extends JpaRepository<PlanSession, UUID> {
    List<PlanSession> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<PlanSession> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

}
