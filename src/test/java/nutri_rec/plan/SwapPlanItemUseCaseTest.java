package nutri_rec.plan;

import nutri_rec.consumption.infrastructure.ConsumptionLogRepository;
import nutri_rec.plan.application.SwapPlanItemUseCase;
import nutri_rec.plan.domain.PlanItem;
import nutri_rec.plan.domain.PlanItemSwap;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.plan.infrastructure.PlanItemSwapRepository;
import nutri_rec.recipe.domain.Recipe;
import nutri_rec.recipe.infrastructure.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SwapPlanItemUseCaseTest {

    @Test
    void test_swap_denied_if_already_consumed() {
        System.out.println("[UT-BE-09] SwapPlanItemUseCase NO permite swap si ya consumido");

        PlanItemRepository planItemRepo = mock(PlanItemRepository.class);
        PlanItemSwapRepository swapRepo = mock(PlanItemSwapRepository.class);
        ConsumptionLogRepository logRepo = mock(ConsumptionLogRepository.class);
        RecipeRepository recipeRepo = mock(RecipeRepository.class);

        SwapPlanItemUseCase uc = new SwapPlanItemUseCase(planItemRepo, swapRepo, logRepo, recipeRepo);

        UUID userId = UUID.randomUUID();
        UUID planItemId = UUID.randomUUID();

        PlanItem item = PlanItem.builder().id(planItemId).recipeId(100).tipoComida("Almuerzo").dia(1).orden(2).planSessionId(UUID.randomUUID()).build();

        when(planItemRepo.findById(planItemId)).thenReturn(Optional.of(item));
        when(logRepo.existsByUserIdAndPlanItemIdAndConsumidoIsTrue(userId, planItemId)).thenReturn(true);

        System.out.println("  Entradas: userId=" + userId + " planItemId=" + planItemId);
        System.out.println("  Esperado: excepción por regla consumido=true");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> uc.execute(userId, planItemId, 200, "quiero cambiar"));

        System.out.println("  Obtenido: " + ex.getMessage());

        verify(swapRepo, never()).save(any(PlanItemSwap.class));
        verify(planItemRepo, never()).save(any(PlanItem.class));
    }

    @Test
    void test_swap_denied_if_new_recipe_not_exists() {
        System.out.println("[UT-BE-10] SwapPlanItemUseCase valida receta nueva exista (Opción B)");

        PlanItemRepository planItemRepo = mock(PlanItemRepository.class);
        PlanItemSwapRepository swapRepo = mock(PlanItemSwapRepository.class);
        ConsumptionLogRepository logRepo = mock(ConsumptionLogRepository.class);
        RecipeRepository recipeRepo = mock(RecipeRepository.class);

        SwapPlanItemUseCase uc = new SwapPlanItemUseCase(planItemRepo, swapRepo, logRepo, recipeRepo);

        UUID userId = UUID.randomUUID();
        UUID planItemId = UUID.randomUUID();

        PlanItem item = PlanItem.builder().id(planItemId).recipeId(100).tipoComida("Almuerzo").dia(1).orden(2).planSessionId(UUID.randomUUID()).build();

        when(planItemRepo.findById(planItemId)).thenReturn(Optional.of(item));
        when(logRepo.existsByUserIdAndPlanItemIdAndConsumidoIsTrue(userId, planItemId)).thenReturn(false);
        when(recipeRepo.findById(999)).thenReturn(Optional.empty());

        System.out.println("  Entrada: newRecipeId=999 (no existe)");
        System.out.println("  Esperado: excepción 'La receta nueva no existe'");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> uc.execute(userId, planItemId, 999, "motivo"));

        System.out.println("  Obtenido: " + ex.getMessage());

        verify(swapRepo, never()).save(any(PlanItemSwap.class));
        verify(planItemRepo, never()).save(any(PlanItem.class));
    }

    @Test
    void test_swap_ok_saves_history_and_updates_item() {
        System.out.println("[UT-BE-11] SwapPlanItemUseCase OK: guarda historial y actualiza item (Opción B)");

        PlanItemRepository planItemRepo = mock(PlanItemRepository.class);
        PlanItemSwapRepository swapRepo = mock(PlanItemSwapRepository.class);
        ConsumptionLogRepository logRepo = mock(ConsumptionLogRepository.class);
        RecipeRepository recipeRepo = mock(RecipeRepository.class);

        SwapPlanItemUseCase uc = new SwapPlanItemUseCase(planItemRepo, swapRepo, logRepo, recipeRepo);

        UUID userId = UUID.randomUUID();
        UUID planItemId = UUID.randomUUID();

        PlanItem item = PlanItem.builder().id(planItemId).recipeId(100).tipoComida("Almuerzo").dia(1).orden(2).planSessionId(UUID.randomUUID()).build();

        when(planItemRepo.findById(planItemId)).thenReturn(Optional.of(item));
        when(logRepo.existsByUserIdAndPlanItemIdAndConsumidoIsTrue(userId, planItemId)).thenReturn(false);
        when(recipeRepo.findById(200)).thenReturn(Optional.of(Recipe.builder().id(200).build()));

        ArgumentCaptor<PlanItemSwap> swapCaptor = ArgumentCaptor.forClass(PlanItemSwap.class);
        ArgumentCaptor<PlanItem> itemCaptor = ArgumentCaptor.forClass(PlanItem.class);

        uc.execute(userId, planItemId, 200, "mejor opción");

        verify(swapRepo).save(swapCaptor.capture());
        verify(planItemRepo).save(itemCaptor.capture());

        PlanItemSwap savedSwap = swapCaptor.getValue();
        PlanItem savedItem = itemCaptor.getValue();

        System.out.println("  Obtenido historial swap:");
        System.out.println("    oldRecipeId=" + savedSwap.getOldRecipeId());
        System.out.println("    newRecipeId=" + savedSwap.getNewRecipeId());
        System.out.println("  Obtenido item actualizado recipeId=" + savedItem.getRecipeId());
        System.out.println("  Esperado: old=100, new=200, item.recipeId=200");

        assertEquals(100, savedSwap.getOldRecipeId());
        assertEquals(200, savedSwap.getNewRecipeId());
        assertEquals(200, savedItem.getRecipeId());
    }
}
