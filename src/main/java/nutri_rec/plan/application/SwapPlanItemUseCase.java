package nutri_rec.plan.application;

import nutri_rec.consumption.infrastructure.ConsumptionLogRepository;
import nutri_rec.plan.domain.PlanItemSwap;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.plan.infrastructure.PlanItemSwapRepository;
import nutri_rec.recipe.infrastructure.RecipeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class SwapPlanItemUseCase {

    private final PlanItemRepository planItemRepo;
    private final PlanItemSwapRepository swapRepo;
    private final ConsumptionLogRepository logRepo;
    private final RecipeRepository recipeRepo;

    public SwapPlanItemUseCase(PlanItemRepository planItemRepo,
                               PlanItemSwapRepository swapRepo,
                               ConsumptionLogRepository logRepo,
                               RecipeRepository recipeRepo) {
        this.planItemRepo = planItemRepo;
        this.swapRepo = swapRepo;
        this.logRepo = logRepo;
        this.recipeRepo = recipeRepo;
    }

    public void execute(UUID userId, UUID planItemId, int newRecipeId, String motivo, LocalDate fecha) {

        if (fecha == null) {
            throw new RuntimeException("La fecha es obligatoria para cambiar una receta.");
        }

        var item = planItemRepo.findById(planItemId)
                .orElseThrow(() -> new RuntimeException("Plan item no encontrado"));

        /* Regla de negocio: NO permitir swap si ya consumió ese item en esa fecha */
        if (logRepo.existsByUserIdAndPlanItemIdAndFechaAndConsumidoIsTrue(userId, planItemId, fecha)) {
            throw new RuntimeException("No puedes cambiar esta receta porque ya fue marcada como consumida en esta fecha.");
        }

        // Validar que la receta exista
        recipeRepo.findById(newRecipeId)
                .orElseThrow(() -> new RuntimeException("La receta nueva no existe"));

        int oldRecipeId = item.getRecipeId();

        // Guardar historial
        swapRepo.save(PlanItemSwap.builder()
                .planItemId(planItemId)
                .userId(userId)
                .oldRecipeId(oldRecipeId)
                .newRecipeId(newRecipeId)
                .motivo(motivo)
                .createdAt(Instant.now())
                .build());

        // Aplicar cambio
        item.setRecipeId(newRecipeId);
        planItemRepo.save(item);
    }
}
