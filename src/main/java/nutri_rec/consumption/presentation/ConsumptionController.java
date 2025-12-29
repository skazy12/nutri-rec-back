package nutri_rec.consumption.presentation;

import nutri_rec.consumption.application.LogConsumptionUseCase;
import nutri_rec.consumption.presentation.dto.LogConsumptionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/consumption")
public class ConsumptionController {

    private final LogConsumptionUseCase logUseCase;

    public ConsumptionController(LogConsumptionUseCase logUseCase) {
        this.logUseCase = logUseCase;
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
}
