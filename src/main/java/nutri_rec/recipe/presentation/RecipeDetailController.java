package nutri_rec.recipe.presentation;

import nutri_rec.recipe.infrastructure.RecipeRepository;
import nutri_rec.recipe.presentation.dto.RecipeDetailResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeDetailController {

    private final RecipeRepository repo;

    public RecipeDetailController(RecipeRepository repo) {
        this.repo = repo;
    }

    // ✅ GET /api/recipes/123  (solo números para no chocar con /search)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {

        var r = repo.findById(id).orElse(null);
        if (r == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(RecipeDetailResponse.from(r));
    }
}
