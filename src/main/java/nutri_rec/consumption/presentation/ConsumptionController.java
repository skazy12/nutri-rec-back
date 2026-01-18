package nutri_rec.consumption.presentation;

import nutri_rec.consumption.application.DeleteConsumptionLogUseCase;
import nutri_rec.consumption.application.GetDailyChecklistUseCase;
import nutri_rec.consumption.application.LogConsumptionUseCase;
import nutri_rec.consumption.infrastructure.ConsumptionLogRepository;
import nutri_rec.consumption.presentation.dto.LogConsumptionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/consumption")
public class ConsumptionController {

    private final LogConsumptionUseCase logUseCase;
    private final DeleteConsumptionLogUseCase deleteUseCase;
    private final GetDailyChecklistUseCase getDailyChecklistUseCase;



    public ConsumptionController(LogConsumptionUseCase logUseCase, DeleteConsumptionLogUseCase deleteUseCase, GetDailyChecklistUseCase getDailyChecklistUseCase) {
        this.logUseCase = logUseCase;
        this.deleteUseCase = deleteUseCase;
        this.getDailyChecklistUseCase = getDailyChecklistUseCase;
    }

    @PostMapping("/log")
    public ResponseEntity<?> log(
            @RequestAttribute("userId") UUID userId,
            @RequestBody LogConsumptionRequest body
    ) {
        if (body.planItemId() == null || body.fecha() == null) {
            throw new RuntimeException("planItemId y fecha son obligatorios");
        }

        var saved = logUseCase.execute(
                userId,
                body.planItemId(),
                body.fecha(),
                body.consumido(),
                body.cantidadPorciones(),
                body.nota()
        );

        return ResponseEntity.ok(saved);
    }
    //  undo borrando el registro de la BDD
    @DeleteMapping("/log")
    public ResponseEntity<?> undo(
            @RequestAttribute("userId") UUID userId,
            @RequestParam("planItemId") UUID planItemId,
            @RequestParam("fecha") LocalDate fecha
    ) {
        deleteUseCase.execute(userId, planItemId, fecha);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/checklist")
    public ResponseEntity<?> checklist(
            @RequestAttribute("userId") UUID userId,
            @RequestParam(required = false) java.time.LocalDate fecha
    ) {
        var res = getDailyChecklistUseCase.execute(userId, fecha);
        return ResponseEntity.ok(res);
    }



}
