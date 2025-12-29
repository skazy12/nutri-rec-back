package nutri_rec.plan.presentation;

import nutri_rec.plan.application.GetAlternativeRecipesUseCase;
import nutri_rec.plan.application.SwapPlanItemUseCase;
import nutri_rec.plan.presentation.dto.RecipeSummaryResponse;
import nutri_rec.plan.presentation.dto.SwapRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/plan-items")
public class PlanItemController {

    private final GetAlternativeRecipesUseCase alternativesUseCase;
    private final SwapPlanItemUseCase swapUseCase;

    public PlanItemController(GetAlternativeRecipesUseCase alternativesUseCase,
                              SwapPlanItemUseCase swapUseCase) {
        this.alternativesUseCase = alternativesUseCase;
        this.swapUseCase = swapUseCase;
    }

    @GetMapping("/{planItemId}/alternatives")
    public ResponseEntity<?> alternatives(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID planItemId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        var list = alternativesUseCase.execute(userId, planItemId, limit)
                .stream()
                .map(RecipeSummaryResponse::from)
                .toList();

        return ResponseEntity.ok(list);
    }

    @PostMapping("/{planItemId}/swap")
    public ResponseEntity<?> swap(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID planItemId,
            @RequestBody SwapRequest body
    ) {
        if (body.newRecipeId() <= 0) {
            throw new RuntimeException("newRecipeId inválido");
        }
        swapUseCase.execute(userId, planItemId, body.newRecipeId(), body.motivo());
        return ResponseEntity.ok().body("swap_ok=true");
    }
}
