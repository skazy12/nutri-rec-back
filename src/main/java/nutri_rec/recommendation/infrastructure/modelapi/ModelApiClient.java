package nutri_rec.recommendation.infrastructure.modelapi;

import java.time.Duration;

import nutri_rec.recommendation.infrastructure.modelapi.dto.ModelApiRequest;
import nutri_rec.recommendation.infrastructure.modelapi.dto.ModelApiResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ModelApiClient {

    private final WebClient webClient;
    private final Duration timeout;

    public ModelApiClient(WebClient modelApiWebClient, ModelApiProperties props) {
        this.webClient = modelApiWebClient;
        this.timeout = Duration.ofMillis(props.timeoutMs());
    }

    public boolean health() {
        try {
            webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(timeout);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ModelApiResponse recomendar(ModelApiRequest request) {
        return webClient.post()
                .uri("/recomendar")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ModelApiResponse.class)
                .block(timeout);
    }
}
