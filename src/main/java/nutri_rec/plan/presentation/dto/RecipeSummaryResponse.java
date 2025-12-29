package nutri_rec.plan.presentation.dto;

import nutri_rec.recipe.domain.Recipe;

public record RecipeSummaryResponse(
        int id,
        String nombre,
        String tipoComida,
        Integer calorias,
        String imagenUrl
) {
    public static RecipeSummaryResponse from(Recipe r) {
        return new RecipeSummaryResponse(
                r.getId(),
                r.getNombre(),
                r.getTipoComida(),
                r.getCalorias(),
                r.getImagen_url()
        );
    }
}
