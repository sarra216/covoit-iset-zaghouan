package tn.iset.covoit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS : quelles autres adresses ont le droit d'appeler l'API.
 * En production, les pages sont servies par Spring Boot lui-même (même adresse) : rien à autoriser de plus.
 * En développement, on autorise Live Server (port 5500). Liste modifiable avec « app.cors.origines ».
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] origines;

    public WebConfig(@Value("${app.cors.origines:http://localhost:5500,http://127.0.0.1:5500}") String origines) {
        this.origines = origines.isBlank() ? new String[0] : origines.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (origines.length == 0) return;
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origines)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Content-Type", "Authorization");
    }
}
