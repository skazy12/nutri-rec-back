package nutri_rec.recipe.presentation;

import nutri_rec.recipe.infrastructure.RecipeRepository;
import nutri_rec.recipe.presentation.dto.RecipeIdsRequest;
import nutri_rec.recipe.presentation.dto.RecipeSearchItemResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
public class RecipeSearchController {

    private final RecipeRepository repo;

    public RecipeSearchController(RecipeRepository repo) {
        this.repo = repo;
    }

    // ✅ GET /api/recipes/search?q=pan&limit=10
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        final String query = (q == null) ? "" : q.trim();

        if (query.length() < 2) {
            // para evitar scans pesados con 1 letra
            return ResponseEntity.ok(List.of());
        }

        // Traemos hasta 20, luego recortamos al limit pedido (1..20)
        int safeLimit = Math.max(1, Math.min(limit, 20));

        var hits = repo.searchByNombreUnaccent(query, safeLimit);

        List<RecipeSearchItemResponse> out = hits.stream()
                .limit(safeLimit)
                .map(r -> new RecipeSearchItemResponse(
                        r.getId(),
                        r.getNombre(),
                        r.getTipoComida(),
                        r.getCategoria_plato(), // OJO: si tu getter es getCategoriaPlato(), cambia aquí
                        r.getImagen_url()       // OJO: si tu entidad tiene imagenUrl
                ))
                .toList();

        return ResponseEntity.ok(out);
    }
    @PostMapping("/lookup")
    public ResponseEntity<?> lookupByIds(@RequestBody RecipeIdsRequest req) {

        if (req == null || req.ids() == null || req.ids().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // limpiamos ids (nulls, repetidos) y ponemos límite para proteger el endpoint
        List<Integer> ids = req.ids().stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(200)  // ajusta si quieres
                .toList();

        var recipes = repo.findByIdIn(ids);

        // Map para reconstruir en el MISMO orden que envió el usuario (muy útil para UI)
        Map<Integer, RecipeSearchItemResponse> mapped = recipes.stream()
                .map(r -> new RecipeSearchItemResponse(
                        r.getId(),
                        r.getNombre(),
                        r.getTipoComida(),
                        r.getCategoria_plato(),  // <-- si tu getter es otro, cámbialo
                        r.getImagen_url()          // <-- si tu getter es otro, cámbialo
                ))
                .collect(Collectors.toMap(RecipeSearchItemResponse::id, x -> x, (a, b) -> a));

        List<RecipeSearchItemResponse> ordered = ids.stream()
                .map(mapped::get)
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(ordered);
    }
}
