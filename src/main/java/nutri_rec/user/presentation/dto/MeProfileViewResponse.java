package nutri_rec.user.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record MeProfileViewResponse(
        String nombre,
        String sexo,
        Integer edad,
        BigDecimal peso,
        BigDecimal talla,
        String nivelActividad,
        Integer comidasDiarias,
        String tipoDieta,
        String objetivoNutricional,
        List<String> restricciones,
        List<Integer> excluirIds,

        // Requerimientos calculados (del user_profile)
        BigDecimal calorias_diarias,
        BigDecimal proteinas_g,
        BigDecimal carbohidratos_g,
        BigDecimal grasas_g,
        BigDecimal calorias_por_comida,
        BigDecimal proteinas_por_comida_g,
        BigDecimal carbohidratos_por_comida_g,
        BigDecimal grasas_por_comida_g,

        // Última sesión del plan
        Integer dias_plan,
        Integer top_n_recetas,
        Boolean almuerzoCenaMisma
) {}
