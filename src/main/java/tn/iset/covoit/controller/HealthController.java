package tn.iset.covoit.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Point de santé de l'application, utilisé par la supervision (Uptime Kuma) et le déploiement.
 * GET /health
 *   200 {"status":"ok","version":"a1b2c3d","base":"ok"}      l'application et la base répondent
 *   503 {"status":"down","version":"a1b2c3d","base":"erreur"} la base de données ne répond plus
 * « version » est le commit déployé (fourni par le pipeline CI) : on voit tout de suite quelle version tourne.
 * Adresse publique (pas de jeton), en dehors de /api.
 */
@RestController
public class HealthController {

    static final String STATUT_OK = "ok";

    private final DataSource dataSource;
    private final String version;

    public HealthController(DataSource dataSource, @Value("${APP_VERSION:dev}") String version) {
        this.dataSource = dataSource;
        this.version = version;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> sante() {
        boolean baseOk = baseDisponible();
        Map<String, String> corps = new LinkedHashMap<>();
        corps.put("status", baseOk ? STATUT_OK : "down");
        corps.put("version", version);
        corps.put("base", baseOk ? "ok" : "erreur");
        return ResponseEntity.status(baseOk ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(corps);
    }

    private boolean baseDisponible() {
        try (Connection connexion = dataSource.getConnection()) {
            return connexion.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
