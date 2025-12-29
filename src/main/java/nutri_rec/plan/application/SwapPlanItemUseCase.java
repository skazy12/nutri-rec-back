package nutri_rec.plan.application;

import nutri_rec.consumption.infrastructure.ConsumptionLogRepository;
import nutri_rec.plan.domain.PlanItemSwap;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.plan.infrastructure.PlanItemSwapRepository;
import nutri_rec.recipe.infrastructure.RecipeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    public void execute(UUID userId, UUID planItemId, int newRecipeId, String motivo) {

        var item = planItemRepo.findById(planItemId)
                .orElseThrow(() -> new RuntimeException("Plan item no encontrado"));

        // Regla de negocio recomendada: NO permitir swap si ya consumió ese item alguna vez.
        if (logRepo.existsByUserIdAndPlanItemIdAndConsumidoIsTrue(userId, planItemId)) {
            throw new RuntimeException("No puedes cambiar esta receta porque ya fue marcada como consumida.");
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
