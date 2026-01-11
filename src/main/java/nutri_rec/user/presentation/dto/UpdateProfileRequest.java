package nutri_rec.user.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProfileRequest(
        String nombre,
        String sexo,
        Integer edad,
        BigDecimal peso,
        BigDecimal talla,
        String nivelActividad,
        Integer comidasDiarias,
        String tipoDieta,
        String objetivoNutricional,
        BigDecimal caloriasDiarias,

        List<String> restricciones,
        List<Integer> excluirIds,
        Boolean almuerzoCenaMisma
) {}
