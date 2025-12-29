package nutri_rec.recipe.presentation.dto;

import java.math.BigDecimal;
import nutri_rec.recipe.domain.Recipe;

public record RecipeDetailResponse(
        Integer id,
        String nombre,
        String tipoComida,
        String categoriaPlato,

        String ingredientes,
        String preparacion,

        Integer calorias,
        BigDecimal proteinas,
        BigDecimal grasas,
        BigDecimal carbohidratos,
        BigDecimal fibra,
        BigDecimal azucares,
        BigDecimal sodio,

        String regionPlato,

        String compatibleVegana,
        String compatibleVegetariana,
        String compatibleBajaCarbo,

        String contieneLactosa,
        String compatibleSinGluten,

        String sinFrutosSecos,
        String bajoEnSodio,
        String altoProteico,
        String bajoEnGrasa,
        String altoEnFibra,
        String aptoDiabetico,

        String fuenteUrl,
        String imagenUrl
) {
    public static RecipeDetailResponse from(Recipe r) {
        return new RecipeDetailResponse(
                r.getId(),
                r.getNombre(),
                r.getTipoComida(),
                r.getCategoria_plato(),

                r.getIngredientes(),
                r.getPreparacion(),

                r.getCalorias(),
                r.getProteinas(),
                r.getGrasas(),
                r.getCarbohidratos(),
                r.getFibra(),
                r.getAzucares(),
                r.getSodio(),

                r.getRegion_plato(),

                r.getCompatible_vegana(),
                r.getCompatible_vegetariana(),
                r.getCompatible_bajacarbo(),

                r.getContiene_lactosa(),
                r.getCompatible_singluten(),

                r.getSin_frutos_secos(),
                r.getBajo_en_sodio(),
                r.getAlto_proteico(),
                r.getBajo_en_grasa(),
                r.getAlto_en_fibra(),
                r.getApto_diabetico(),

                r.getFuente_url(),
                r.getImagen_url()
        );
    }
}
