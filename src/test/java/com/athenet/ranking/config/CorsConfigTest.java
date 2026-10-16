package com.athenet.ranking.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class CorsConfigTest {

    /** CorsRegistry no expone sus mapeos; se leen desde una subclase. */
    private static class RegistryLegible extends CorsRegistry {
        Map<String, CorsConfiguration> mapeos() {
            return getCorsConfigurations();
        }
    }

    @Test
    void registraOrigenesSinEspaciosSoloParaLaApiYSoloLectura() {
        CorsConfig config = new CorsConfig(List.of(" http://localhost:5173", "https://fabetabilo.github.io ", " "));
        RegistryLegible registry = new RegistryLegible();

        config.addCorsMappings(registry);

        CorsConfiguration cors = registry.mapeos().get("/api/**");
        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins())
                .containsExactly("http://localhost:5173", "https://fabetabilo.github.io");
        assertThat(cors.getAllowedMethods()).containsExactlyInAnyOrder("GET", "HEAD", "OPTIONS");
    }
}
