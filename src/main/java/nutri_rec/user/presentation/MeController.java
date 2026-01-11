package nutri_rec.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import nutri_rec.plan.infrastructure.PlanSessionRepository;
import nutri_rec.user.infrastructure.UserProfileRepository;
import nutri_rec.user.presentation.dto.MeProfileViewResponse;
import nutri_rec.user.presentation.dto.UpdateProfileRequest;
import nutri_rec.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserProfileRepository profileRepo;
    private final PlanSessionRepository sessionRepo;

    public MeController(UserProfileRepository profileRepo,
                        PlanSessionRepository sessionRepo) {
        this.profileRepo = profileRepo;
        this.sessionRepo = sessionRepo;
    }

    @GetMapping
    public ResponseEntity<?> me(@RequestAttribute("userId") UUID userId) {
        var profile = profileRepo.findByUserId(userId).orElse(null);
        return ResponseEntity.ok(profile);
    }
    @GetMapping("/profile")
    public ResponseEntity<?> myProfile(@RequestAttribute("userId") UUID userId) {

        var profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        var restricciones = JsonUtils.parseStringList(profile.getRestriccionesJson());
        var excluirIds = JsonUtils.parseIntegerList(profile.getExcluirIdsJson());

        var latestSession = sessionRepo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        Integer diasPlan = (latestSession == null) ? null : latestSession.getDiasPlan();
        Integer topN = (latestSession == null) ? null : latestSession.getTopNRecetas();

        return ResponseEntity.ok(new MeProfileViewResponse(
                // Base
                profile.getNombre(),
                profile.getSexo(),
                profile.getEdad(),
                profile.getPeso(),
                profile.getTalla(),
                profile.getNivelActividad(),
                profile.getComidasDiarias(),
                profile.getTipoDieta(),
                profile.getObjetivoNutricional(),
                restricciones,
                excluirIds,

                // Requerimientos (OJO: asegúrate que estos getters existan en tu entidad)
                profile.getCaloriasDiarias(),
                profile.getProteinasG(),
                profile.getCarbohidratosG(),
                profile.getGrasasG(),
                profile.getCaloriasPorComida(),
                profile.getProteinasPorComidaG(),
                profile.getCarbohidratosPorComidaG(),
                profile.getGrasasPorComidaG(),

                // Última sesión
                diasPlan,
                topN,
                profile.getAlmuerzoCenaMisma()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestAttribute("userId") UUID userId,
                                           @RequestBody UpdateProfileRequest body) throws Exception {

        var profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        profile.setNombre(body.nombre());
        profile.setSexo(body.sexo());
        profile.setEdad(body.edad());
        profile.setPeso(body.peso());
        profile.setTalla(body.talla());
        profile.setNivelActividad(body.nivelActividad());
        profile.setComidasDiarias(body.comidasDiarias());
        profile.setTipoDieta(body.tipoDieta());
        profile.setObjetivoNutricional(body.objetivoNutricional());
        profile.setAlmuerzoCenaMisma(body.almuerzoCenaMisma() != null ? body.almuerzoCenaMisma() : false);


        var om = new ObjectMapper();

        profile.setRestriccionesJson(
                om.writeValueAsString(body.restricciones() == null ? java.util.List.of() : body.restricciones())
        );

        profile.setExcluirIdsJson(
                om.writeValueAsString(body.excluirIds() == null ? java.util.List.of() : body.excluirIds())
        );

        profileRepo.save(profile);
        return ResponseEntity.ok(profile);
    }
}
