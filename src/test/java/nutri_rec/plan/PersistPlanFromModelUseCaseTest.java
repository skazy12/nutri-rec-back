package nutri_rec.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nutri_rec.plan.application.PersistPlanFromModelUseCase;
import nutri_rec.plan.domain.PlanItem;
import nutri_rec.plan.domain.PlanSession;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.plan.infrastructure.PlanSessionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PersistPlanFromModelUseCaseTest {

    @Test
    void test_execute_creates_session_and_items_with_correct_order() throws Exception {
        System.out.println("[UT-BE-08] PersistPlanFromModelUseCase crea session + items con orden correcto ");

        PlanSessionRepository sessionRepo = mock(PlanSessionRepository.class);
        PlanItemRepository itemRepo = mock(PlanItemRepository.class);

        PersistPlanFromModelUseCase uc = new PersistPlanFromModelUseCase(sessionRepo, itemRepo);

        UUID userId = UUID.randomUUID();

        // sessionRepo.save devuelve la session con id
        when(sessionRepo.save(any(PlanSession.class))).thenAnswer(inv -> {
            PlanSession s = inv.getArgument(0);
            if (s.getId() == null) s.setId(UUID.randomUUID());
            return s;
        });

        ObjectMapper om = new ObjectMapper();

        // planArrayNode: 1 día con Desayuno/Almuerzo/Cena/Snack1/Snack2 (para validar orden snackCounter)
        String planJson = """
        [
          {"Dia":1,"Comida":"Desayuno","ID_Receta":101},
          {"Dia":1,"Comida":"Almuerzo","ID_Receta":102},
          {"Dia":1,"Comida":"Cena","ID_Receta":103},
          {"Dia":1,"Comida":"Snack1","ID_Receta":104},
          {"Dia":1,"Comida":"Snack2","ID_Receta":105},
          {"Dia":2,"Comida":"Snack","ID_Receta":106}
        ]
        """;
        JsonNode planArray = om.readTree(planJson);
        JsonNode fullResponse = om.readTree("{\"ok\":true}");

        ArgumentCaptor<PlanItem> itemCaptor = ArgumentCaptor.forClass(PlanItem.class);

        UUID sessionId = uc.execute(
                userId,
                7,
                50,
                List.of("sin gluten"),
                List.of(1,2),
                fullResponse,
                planArray
        );

        verify(sessionRepo, times(1)).save(any(PlanSession.class));
        verify(itemRepo, times(6)).save(itemCaptor.capture());

        List<PlanItem> savedItems = itemCaptor.getAllValues();

        System.out.println("  Entrada: plan con Dia=1 (Des/Alm/Cena/Snack1/Snack2) y Dia=2 (Snack)");
        System.out.println("  Obtenido: sessionId=" + sessionId);
        System.out.println("  Obtenido items (dia, comida, recipeId, orden):");
        savedItems.forEach(i -> System.out.println(
                "    dia=" + i.getDia() +
                        " comida=" + i.getTipoComida() +
                        " recipeId=" + i.getRecipeId() +
                        " orden=" + i.getOrden()
        ));

        System.out.println("  Esperado orden:");
        System.out.println("    Desayuno=1, Almuerzo=2, Cena=3, Snack1=4, Snack2=5, Dia2 Snack=4");

        assertNotNull(sessionId);

        // validar algunos ordenes clave
        PlanItem desayuno = savedItems.get(0);
        PlanItem almuerzo = savedItems.get(1);
        PlanItem cena = savedItems.get(2);
        PlanItem snack1 = savedItems.get(3);
        PlanItem snack2 = savedItems.get(4);
        PlanItem dia2snack = savedItems.get(5);

        assertEquals(1, desayuno.getOrden());
        assertEquals(2, almuerzo.getOrden());
        assertEquals(3, cena.getOrden());
        assertEquals(4, snack1.getOrden());
        assertEquals(5, snack2.getOrden());

        // al cambiar de día se reinicia snackCounter => Snack debe volver a 4
        assertEquals(2, dia2snack.getDia());
        assertEquals(4, dia2snack.getOrden());
    }
}
