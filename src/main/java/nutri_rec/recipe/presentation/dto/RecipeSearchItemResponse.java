package nutri_rec.recipe.presentation.dto;

public record RecipeSearchItemResponse(
        Integer id,
        String nombre,
        String tipoComida,
        String categoriaPlato,
        String imagenUrl
) {}
