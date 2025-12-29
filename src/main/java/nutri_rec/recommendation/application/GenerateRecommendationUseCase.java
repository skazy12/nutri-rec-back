package nutri_rec.recommendation.application;

import nutri_rec.recommendation.domain.RecommendationResult;
import nutri_rec.recommendation.infrastructure.modelapi.ModelApiClient;
import nutri_rec.recommendation.infrastructure.modelapi.dto.ModelApiRequest;
import org.springframework.stereotype.Service;

@Service
public class GenerateRecommendationUseCase {

    private final ModelApiClient client;

    public GenerateRecommendationUseCase(ModelApiClient client) {
        this.client = client;
    }

    public RecommendationResult execute(ModelApiRequest request) {
        var resp = client.recomendar(request);
        return new RecommendationResult(resp.requerimientos(), resp.recomendaciones(), resp.plan());

    }
}
