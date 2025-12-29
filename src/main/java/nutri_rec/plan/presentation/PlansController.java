package nutri_rec.plan.presentation;

import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.plan.infrastructure.PlanSessionRepository;
import nutri_rec.plan.presentation.dto.PlanItemResponse;
import nutri_rec.plan.presentation.dto.PlanItemWithRecipeResponse;
import nutri_rec.recipe.infrastructure.RecipeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
public class PlansController {

    private final PlanSessionRepository sessionRepo;
    private final PlanItemRepository itemRepo;
    private final RecipeRepository recipeRepo;

    public PlansController(PlanSessionRepository sessionRepo,
                           PlanItemRepository itemRepo,
                           RecipeRepository recipeRepo) {
        this.sessionRepo = sessionRepo;
        this.itemRepo = itemRepo;
        this.recipeRepo = recipeRepo;
    }

    // GET /api/plans/latest
    @GetMapping("/latest")
    public ResponseEntity<?> latest(@RequestAttribute("userId") UUID userId) {

        var latestSession = sessionRepo.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElse(null);

        if (latestSession == null) {
            return ResponseEntity.notFound().build();
        }

        var planItems = itemRepo.findByPlanSessionIdOrderByDiaAscOrdenAsc(latestSession.getId());

        // 1) juntar recipeIds
        var recipeIds = planItems.stream()
                .map(pi -> pi.getRecipeId())
                .distinct()
                .toList();

        // 2) traer recetas y armar map (id -> recipe)
        var recipesMap = recipeRepo.findAllById(recipeIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        r -> r.getId(),
                        r -> r
                ));

        // 3) mapear items enriquecidos
        var items = planItems.stream()
                .map(pi -> PlanItemWithRecipeResponse.from(pi, recipesMap.get(pi.getRecipeId())))
                .toList();

        return ResponseEntity.ok(new Object() {
            public final UUID sessionId = latestSession.getId();
            public final Object session = latestSession;
            public final Object itemsList = items;
        });
    }

    // GET /api/plans/{sessionId}/items
    @GetMapping("/{sessionId}/items")
    public ResponseEntity<?> items(@RequestAttribute("userId") UUID userId,
                                   @PathVariable UUID sessionId) {

        var session = sessionRepo.findById(sessionId).orElse(null);
        if (session == null) return ResponseEntity.notFound().build();

        // seguridad: evitar que un usuario consulte sesiones de otro
        if (!session.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("No autorizado para esta sesión");
        }

        var items = itemRepo.findByPlanSessionIdOrderByDiaAscOrdenAsc(sessionId)
                .stream()
                .map(PlanItemResponse::from)
                .toList();

        return ResponseEntity.ok(items);
    }
}
