package nutri_rec.plan.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.recipe.domain.Recipe;
import nutri_rec.recipe.infrastructure.RecipeRepository;
import nutri_rec.user.infrastructure.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GetAlternativeRecipesUseCase {

    private final PlanItemRepository planItemRepo;
    private final UserProfileRepository profileRepo;
    private final RecipeRepository recipeRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetAlternativeRecipesUseCase(PlanItemRepository planItemRepo,
                                        UserProfileRepository profileRepo,
                                        RecipeRepository recipeRepo) {
        this.planItemRepo = planItemRepo;
        this.profileRepo = profileRepo;
        this.recipeRepo = recipeRepo;
    }

    public List<Recipe> execute(UUID userId, UUID planItemId, int limit) {

        var profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        var item = planItemRepo.findById(planItemId)
                .orElseThrow(() -> new RuntimeException("Plan item no encontrado"));

        var baseRecipe = recipeRepo.findById(item.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Receta base no encontrada"));

        List<String> restricciones = parseStringList(profile.getRestriccionesJson());
        List<Integer> excluirIds = parseIntegerList(profile.getExcluirIdsJson());

        // 1) candidatos por tipo_comida (usamos el slot del plan_item: Desayuno/Almuerzo/Cena/Snack...)
        String tipo = normalizeTipo(item.getTipoComida());
        List<Recipe> candidatos = recipeRepo.findByTipoComidaIgnoreCase(tipo);

        // 2) filtros duros: excluir ids + dieta + restricciones
        candidatos = candidatos.stream()
                .filter(r -> !Objects.equals(r.getId(), baseRecipe.getId()))
                .filter(r -> excluirIds == null || !excluirIds.contains(r.getId()))
                .filter(r -> isAllowedByDiet(profile.getTipoDieta(), r))
                .filter(r -> isAllowedByRestrictions(restricciones, r))
                .collect(Collectors.toList());

        // 3) similitud simple por macros/calorías (score menor = más similar)
        candidatos.sort(Comparator.comparingDouble(r -> distance(baseRecipe, r)));

        return candidatos.stream().limit(Math.max(1, limit)).toList();
    }

    private String normalizeTipo(String slot) {
        // El modelo usa Comida slot, pero en recipe.tipo_comida tienes Desayuno/Almuerzo/Cena/Merienda
        if (slot == null) return "Merienda";
        String s = slot.trim().toLowerCase();
        if (s.startsWith("desayuno")) return "Desayuno";
        if (s.startsWith("almuerzo")) return "Almuerzo";
        if (s.startsWith("cena")) return "Cena";
        return "Merienda"; // Snack/Snack1/Snack2 -> Merienda
    }

    private boolean isAllowedByDiet(String tipoDieta, Recipe r) {
        if (tipoDieta == null) return true;
        String d = tipoDieta.trim().toLowerCase();

        if (d.contains("vegana")) return isYes(r.getCompatible_vegana());
        if (d.contains("vegetar")) return isYes(r.getCompatible_vegetariana());
        if (d.contains("baja") && d.contains("carb")) return isYes(r.getCompatible_bajacarbo());

        return true; // Normal u otras
    }

    private boolean isAllowedByRestrictions(List<String> restricciones, Recipe r) {
        if (restricciones == null || restricciones.isEmpty()) return true;

        for (String raw : restricciones) {
            if (raw == null) continue;
            String x = raw.trim().toLowerCase();

            if (x.contains("sin lactosa")) {
                // si contiene_lactosa = "Sí" => NO permitido
                if (isYes(r.getContiene_lactosa())) return false;
            }
            if (x.contains("sin gluten")) {
                if (!isYes(r.getCompatible_singluten())) return false;
            }
            if (x.contains("hipertenso")) {
                if (!isYes(r.getBajo_en_sodio())) return false;
            }
            if (x.contains("diab")) {
                if (!isYes(r.getApto_diabetico())) return false;
            }
            if (x.contains("frutos secos") || x.contains("nueces")) {
                if (!isYes(r.getSin_frutos_secos())) return false;
            }
        }
        return true;
    }

    private boolean isYes(String v) {
        if (v == null) return false;
        String s = v.trim().toLowerCase();
        return s.equals("si") || s.equals("sí") || s.equals("true") || s.equals("1") || s.equals("y") || s.equals("yes");
    }

    private double distance(Recipe a, Recipe b) {
        // pesos simples: calorías 2x, macros 1x
        double calA = a.getCalorias() == null ? 0 : a.getCalorias();
        double calB = b.getCalorias() == null ? 0 : b.getCalorias();

        double pA = bd(a.getProteinas());
        double pB = bd(b.getProteinas());
        double gA = bd(a.getGrasas());
        double gB = bd(b.getGrasas());
        double cA = bd(a.getCarbohidratos());
        double cB = bd(b.getCarbohidratos());

        return (Math.abs(calA - calB) * 2.0)
                + Math.abs(pA - pB)
                + Math.abs(gA - gB)
                + Math.abs(cA - cB);
    }

    private double bd(BigDecimal x) {
        return x == null ? 0.0 : x.doubleValue();
    }

    private List<String> parseStringList(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<Integer> parseIntegerList(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
