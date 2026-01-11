package nutri_rec.plan;

import nutri_rec.plan.application.GetAlternativeRecipesUseCase;
import nutri_rec.plan.domain.PlanItem;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.recipe.domain.Recipe;
import nutri_rec.recipe.infrastructure.RecipeRepository;
import nutri_rec.user.domain.UserProfile;
import nutri_rec.user.infrastructure.UserProfileRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetAlternativeRecipesUseCaseTest {

    @Test
    void test_alternatives_filters_and_sorts_by_similarity() {
        System.out.println("[UT-BE-12] GetAlternativeRecipesUseCase filtra y ordena por similitud");

        PlanItemRepository planItemRepo = mock(PlanItemRepository.class);
        UserProfileRepository profileRepo = mock(UserProfileRepository.class);
        RecipeRepository recipeRepo = mock(RecipeRepository.class);

        GetAlternativeRecipesUseCase uc = new GetAlternativeRecipesUseCase(planItemRepo, profileRepo, recipeRepo);

        UUID userId = UUID.randomUUID();
        UUID planItemId = UUID.randomUUID();

        // Perfil: vegana + sin gluten, excluye ID=30
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .tipoDieta("Vegana")
                .restriccionesJson("[\"sin gluten\"]")
                .excluirIdsJson("[30]")
                .build();

        when(profileRepo.findByUserId(userId)).thenReturn(Optional.of(profile));

        PlanItem item = PlanItem.builder().id(planItemId).recipeId(10).tipoComida("Almuerzo").dia(1).orden(2).planSessionId(UUID.randomUUID()).build();
        when(planItemRepo.findById(planItemId)).thenReturn(Optional.of(item));

        // Receta base (id=10)
        Recipe base = Recipe.builder()
                .id(10).tipoComida("Almuerzo")
                .calorias(600)
                .proteinas(BigDecimal.valueOf(25))
                .grasas(BigDecimal.valueOf(20))
                .carbohidratos(BigDecimal.valueOf(70))
                .build();
        when(recipeRepo.findById(10)).thenReturn(Optional.of(base));

        // Candidatos por tipo comida (Almuerzo)
        Recipe r20 = Recipe.builder()
                .id(20).tipoComida("Almuerzo")
                .calorias(605)
                .proteinas(BigDecimal.valueOf(24))
                .grasas(BigDecimal.valueOf(19))
                .carbohidratos(BigDecimal.valueOf(71))
                .compatible_vegana("Si")
                .compatible_singluten("Si")
                .build();

        // Excluido por excluirIdsJson
        Recipe r30 = Recipe.builder()
                .id(30).tipoComida("Almuerzo")
                .calorias(590)
                .proteinas(BigDecimal.valueOf(26))
                .grasas(BigDecimal.valueOf(20))
                .carbohidratos(BigDecimal.valueOf(69))
                .compatible_vegana("Si")
                .compatible_singluten("Si")
                .build();

        // No cumple dieta (no vegana)
        Recipe r40 = Recipe.builder()
                .id(40).tipoComida("Almuerzo")
                .calorias(600)
                .proteinas(BigDecimal.valueOf(25))
                .grasas(BigDecimal.valueOf(20))
                .carbohidratos(BigDecimal.valueOf(70))
                .compatible_vegana("No")
                .compatible_singluten("Si")
                .build();

        // Cumple filtros pero menos similar (más distancia)
        Recipe r50 = Recipe.builder()
                .id(50).tipoComida("Almuerzo")
                .calorias(800)
                .proteinas(BigDecimal.valueOf(10))
                .grasas(BigDecimal.valueOf(40))
                .carbohidratos(BigDecimal.valueOf(120))
                .compatible_vegana("Si")
                .compatible_singluten("Si")
                .build();

        when(recipeRepo.findByTipoComidaIgnoreCase("Almuerzo"))
                .thenReturn(List.of(base, r20, r30, r40, r50));

        List<Recipe> out = uc.execute(userId, planItemId, 5);

        System.out.println("  Entradas:");
        System.out.println("    dieta=Vegana, restricciones=[sin gluten], excluirIds=[30], tipo=Almuerzo");
        System.out.println("  Obtenido IDs (ordenados por similitud): " + out.stream().map(Recipe::getId).toList());
        System.out.println("  Esperado:");
        System.out.println("    - no incluye base (10)");
        System.out.println("    - no incluye excluido (30)");
        System.out.println("    - no incluye no-vegana (40)");
        System.out.println("    - primero r20 por ser más similar que r50");

        List<Integer> ids = out.stream().map(Recipe::getId).toList();

        assertFalse(ids.contains(10));
        assertFalse(ids.contains(30));
        assertFalse(ids.contains(40));

        assertEquals(20, ids.get(0));
        assertTrue(ids.contains(50));
    }
}
