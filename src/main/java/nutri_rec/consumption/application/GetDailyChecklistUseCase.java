package nutri_rec.consumption.application;

import nutri_rec.consumption.infrastructure.ConsumptionLogRepository;
import nutri_rec.consumption.presentation.dto.DailyChecklistItemResponse;
import nutri_rec.consumption.presentation.dto.DailyChecklistResponse;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.plan.infrastructure.PlanSessionRepository;
import nutri_rec.recipe.infrastructure.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GetDailyChecklistUseCase {

    private final PlanSessionRepository sessionRepo;
    private final PlanItemRepository itemRepo;
    private final RecipeRepository recipeRepo;
    private final ConsumptionLogRepository logRepo;

    public GetDailyChecklistUseCase(
            PlanSessionRepository sessionRepo,
            PlanItemRepository itemRepo,
            RecipeRepository recipeRepo,
            ConsumptionLogRepository logRepo
    ) {
        this.sessionRepo = sessionRepo;
        this.itemRepo = itemRepo;
        this.recipeRepo = recipeRepo;
        this.logRepo = logRepo;
    }

    /**
     * Devuelve el checklist de un día: items recomendados (PlanItem) + datos de Recipe + estado consumido.
     * - Si no existe log para un item en esa fecha, consumido=false.
     */
    @Transactional(readOnly = true)
    public DailyChecklistResponse execute(UUID userId, LocalDate fecha) {
        LocalDate targetDate = (fecha != null) ? fecha : LocalDate.now();

        var latestSession = sessionRepo.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No existe plan reciente para el usuario"));

        LocalDate startDate = latestSession.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        int diaPlan = (int) ChronoUnit.DAYS.between(startDate, targetDate) + 1;

        if (diaPlan < 1 || diaPlan > latestSession.getDiasPlan()) {
            return new DailyChecklistResponse(latestSession.getId(), targetDate, diaPlan, List.of());
        }

        var planItems = itemRepo.findByPlanSessionIdAndDiaOrderByOrdenAsc(latestSession.getId(), diaPlan);

        if (planItems.isEmpty()) {
            return new DailyChecklistResponse(latestSession.getId(), targetDate, diaPlan, List.of());
        }

        // 1) Recipe map (igual que tu /latest)
        var recipeIds = planItems.stream().map(pi -> pi.getRecipeId()).distinct().toList();

        var recipesMap = recipeRepo.findAllById(recipeIds).stream()
                .collect(Collectors.toMap(r -> r.getId(), r -> r));

        // 2) Logs map (planItemId -> log) para esa fecha
        var planItemIds = planItems.stream().map(pi -> pi.getId()).toList();
        var logs = logRepo.findByUserIdAndPlanItemIdInAndFecha(userId, planItemIds, targetDate);

        var logsMap = logs.stream()
                .collect(Collectors.toMap(l -> l.getPlanItemId(), l -> l, (a, b) -> a));

        // 3) Merge final para UI
        var items = planItems.stream().map(pi -> {
            var recipe = recipesMap.get(pi.getRecipeId());
            var log = logsMap.get(pi.getId());

            boolean consumido = log != null && Boolean.TRUE.equals(log.getConsumido());

            return new DailyChecklistItemResponse(
                    pi.getId(),
                    pi.getDia(),
                    pi.getOrden(),
                    pi.getTipoComida(),
                    pi.getRecipeId(),
                    (recipe != null) ? recipe.getNombre() : null,
                    (recipe != null) ? recipe.getImagen_url() : null,
                    consumido,
                    (log != null) ? log.getCantidadPorciones() : null,
                    (log != null) ? log.getNota() : null
            );
        }).toList();

        return new DailyChecklistResponse(latestSession.getId(), targetDate, diaPlan, items);
    }
}
