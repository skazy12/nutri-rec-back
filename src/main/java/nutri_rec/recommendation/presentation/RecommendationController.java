package nutri_rec.recommendation.presentation;

import nutri_rec.plan.application.PersistPlanFromModelUseCase;


import com.fasterxml.jackson.databind.JsonNode;
import nutri_rec.recommendation.application.GenerateRecommendationUseCase;
import nutri_rec.recommendation.domain.RecommendationResult;
import nutri_rec.recommendation.infrastructure.modelapi.dto.ModelApiRequest;
import nutri_rec.user.infrastructure.UserProfileRepository;
import nutri_rec.plan.infrastructure.PlanItemRepository;
import nutri_rec.plan.presentation.dto.PlanItemResponse;
import nutri_rec.recommendation.presentation.dto.ForMePlanResponse;


import nutri_rec.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {

    private final GenerateRecommendationUseCase useCase;
    private final UserProfileRepository profileRepo;
    private final PersistPlanFromModelUseCase persistPlanUseCase;
    private final PlanItemRepository planItemRepo;





    public RecommendationController(
            GenerateRecommendationUseCase useCase,
            UserProfileRepository profileRepo,
            PersistPlanFromModelUseCase persistPlanUseCase,
            PlanItemRepository planItemRepo
    ) {
        this.useCase = useCase;
        this.profileRepo = profileRepo;
        this.persistPlanUseCase = persistPlanUseCase;
        this.planItemRepo = planItemRepo;
    }


    // Endpoint para obtener recomendaciones personalizadas para el usuario autenticado
    @GetMapping("/for-me")
    public ResponseEntity<ForMePlanResponse> recommendForMe(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(defaultValue = "7") int dias,
            @RequestParam(defaultValue = "30") int topN
    ) {
        var profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));
        var restricciones = JsonUtils.parseStringList(profile.getRestriccionesJson());
        var excluirIds = JsonUtils.parseIntegerList(profile.getExcluirIdsJson());

        if (profile.getSexo() == null || profile.getEdad() == null ||
                profile.getPeso() == null || profile.getTalla() == null ||
                profile.getNivelActividad() == null || profile.getComidasDiarias() == null ||
                profile.getTipoDieta() == null || profile.getObjetivoNutricional() == null) {
            throw new RuntimeException("Completa tu perfil antes de pedir recomendaciones (sexo, edad, peso, talla, actividad, comidas, dieta, objetivo).");
        }

        var req = new ModelApiRequest(
                profile.getSexo(),
                profile.getEdad(),
                profile.getPeso().doubleValue(),
                profile.getTalla().doubleValue(),
                profile.getNivelActividad(),
                profile.getComidasDiarias(),
                profile.getTipoDieta(),
                restricciones,
                profile.getObjetivoNutricional(),
                dias,
                topN,
                excluirIds
        );

        var result = useCase.execute(req);

        guardarRequerimientosEnPerfil(profile, result.requerimientos());
        profileRepo.save(profile);

        var sessionId = persistPlanUseCase.execute(
                userId,
                dias,
                topN,
                restricciones,
                excluirIds,
                null,
                result.plan()
        );

        var items = planItemRepo.findByPlanSessionIdOrderByDiaAscOrdenAsc(sessionId)
                .stream()
                .map(PlanItemResponse::from)
                .toList();

        return ResponseEntity.ok(new ForMePlanResponse(
                sessionId,
                items,
                result.plan()
        ));
    }
    // Endpoint de health check del modelo
    @GetMapping("/health")
    public ResponseEntity<?> modelHealth() {
        return ResponseEntity.ok().body("model_alive=" + true);
    }






    // Guarda los requerimientos nutricionales del modelo en el perfil del usuario
    private void guardarRequerimientosEnPerfil(nutri_rec.user.domain.UserProfile profile, JsonNode req) {
        if (req == null || req.isNull()) return;

        profile.setCaloriasDiarias(asBigDecimal(req, "calorias_diarias"));
        profile.setProteinasG(asBigDecimal(req, "proteinas_g"));
        profile.setCarbohidratosG(asBigDecimal(req, "carbohidratos_g"));
        profile.setGrasasG(asBigDecimal(req, "grasas_g"));

        profile.setCaloriasPorComida(asBigDecimal(req, "calorias_por_comida"));
        profile.setProteinasPorComidaG(asBigDecimal(req, "proteinas_por_comida_g"));
        profile.setCarbohidratosPorComidaG(asBigDecimal(req, "carbohidratos_por_comida_g"));
        profile.setGrasasPorComidaG(asBigDecimal(req, "grasas_por_comida_g"));
    }
    // Convierte un campo JsonNode a BigDecimal, aceptando int/float/string
    private BigDecimal asBigDecimal(JsonNode node, String field) {
        var v = node.get(field);
        if (v == null || v.isNull()) return null;

        // acepta int/float/string
        if (v.isNumber()) return v.decimalValue();
        if (v.isTextual()) {
            try { return new BigDecimal(v.asText().trim()); } catch (Exception ignored) {}
        }
        return null;
    }

}
