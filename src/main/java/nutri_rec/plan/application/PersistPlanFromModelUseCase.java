package nutri_rec.plan.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nutri_rec.plan.domain.PlanItem;
import nutri_rec.plan.domain.PlanSession;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.plan.infrastructure.PlanSessionRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class PersistPlanFromModelUseCase {

    private final PlanSessionRepository sessionRepo;
    private final PlanItemRepository itemRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PersistPlanFromModelUseCase(PlanSessionRepository sessionRepo, PlanItemRepository itemRepo) {
        this.sessionRepo = sessionRepo;
        this.itemRepo = itemRepo;
    }

    public UUID execute(UUID userId,
                        int dias,
                        int topN,
                        List<String> restricciones,
                        List<Integer> excluirIds,
                        JsonNode modelFullResponse,
                        JsonNode planArrayNode) {

        String restriccionesJson = safeWriteJson(restricciones);
        String excluirIdsJson = safeWriteJson(excluirIds);

        var session = PlanSession.builder()
                .userId(userId)
                .diasPlan(dias)
                .topNRecetas(topN)
                .restriccionesJson(restriccionesJson)
                .excluirIdsJson(excluirIdsJson)
                .payloadMode("for-me")
                .respuestaMo(modelFullResponse == null ? null : modelFullResponse.toString())
                .createdAt(Instant.now())
                .build();

        session = sessionRepo.save(session);

        // planArrayNode esperado: array de objetos con "Dia" y "Comida" y "ID_Receta"
        if (planArrayNode != null && planArrayNode.isArray()) {
            int snackCounterPerDay = 0;
            int currentDay = -1;

            for (JsonNode row : planArrayNode) {
                int dia = row.path("Dia").asInt();
                String comida = row.path("Comida").asText(); // "Desayuno", "Almuerzo", "Cena", "Snack", "Snack1", "Snack2"
                int recipeId = row.path("ID_Receta").asInt();

                // Reset por día
                if (dia != currentDay) {
                    currentDay = dia;
                    snackCounterPerDay = 0;
                }

                // ✅ Snack counter SOLO para snacks
                int orden;
                if (isSnack(comida)) {
                    snackCounterPerDay++;
                    orden = computeOrden(comida, snackCounterPerDay); // snacks: 4,5,...
                } else {
                    orden = computeOrden(comida, 0); // desayuno/almuerzo/cena
                }

                itemRepo.save(PlanItem.builder()
                        .planSessionId(session.getId())
                        .dia(dia)
                        .tipoComida(comida)
                        .recipeId(recipeId)
                        .orden(orden)
                        .createdAt(Instant.now())
                        .build());
            }
        }

        return session.getId();
    }

    private boolean isSnack(String comida) {
        String c = comida == null ? "" : comida.trim().toLowerCase();
        return !(c.startsWith("desayuno") || c.startsWith("almuerzo") || c.startsWith("cena"));
    }

    private int computeOrden(String comida, int snackIndex) {
        // orden estándar para UI: Desayuno(1) Almuerzo(2) Cena(3) Snacks(4+)
        String c = comida == null ? "" : comida.trim().toLowerCase();
        if (c.startsWith("desayuno")) return 1;
        if (c.startsWith("almuerzo")) return 2;
        if (c.startsWith("cena")) return 3;
        return 3 + snackIndex; // snackIndex=1 => 4, snackIndex=2 => 5, ...
    }

    private String safeWriteJson(Object obj) {
        try {
            if (obj == null) return "[]";
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }
}
