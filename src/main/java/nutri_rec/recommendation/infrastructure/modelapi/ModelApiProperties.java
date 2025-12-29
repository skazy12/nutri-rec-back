package nutri_rec.recommendation.infrastructure.modelapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "modelapi")
public record ModelApiProperties(
        String baseUrl,
        long timeoutMs
) {}
