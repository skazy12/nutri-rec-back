package nutri_rec.recipe.infrastructure;

import nutri_rec.recipe.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    List<Recipe> findByTipoComidaIgnoreCase(String tipoComida);

    @Query(value = """
        SELECT *
        FROM recipe r
        WHERE unaccent(lower(r.nombre)) LIKE unaccent(lower(concat('%', :q, '%')))
        ORDER BY
          CASE
            WHEN unaccent(lower(r.nombre)) = unaccent(lower(:q)) THEN 0
            WHEN unaccent(lower(r.nombre)) LIKE unaccent(lower(concat(:q, '%'))) THEN 1
            ELSE 2
          END,
          length(r.nombre) ASC,
          r.nombre ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Recipe> searchByNombreUnaccent(
            @Param("q") String q,
            @Param("limit") int limit
    );

    List<Recipe> findByIdIn(List<Integer> ids);

}
