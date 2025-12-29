package nutri_rec.config;

import nutri_rec.recommendation.infrastructure.modelapi.ModelApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ModelApiProperties.class)
public class ModelApiConfig {}
