package tn.iset.covoit.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String CLE = "une-cle-secrete-de-test-assez-longue-123456";
    private static final Clock MAINTENANT = Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);

    private final JwtService service = new JwtService(CLE, Duration.ofHours(24), MAINTENANT);

    @Test
    void unJetonValideDonneLIdDeLUtilisateur() {
        String jeton = service.creerJeton(42L);

        assertEquals(3, jeton.split("\\.").length, "en-tête.contenu.signature");
        assertEquals(42L, service.verifier(jeton));
    }

    @Test
    void unJetonModifieEstRefuse() {
        String[] parties = service.creerJeton(42L).split("\\.");
        String contenuFalsifie = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"1\",\"iat\":0,\"exp\":9999999999}".getBytes(StandardCharsets.UTF_8));

        assertNull(service.verifier(parties[0] + "." + contenuFalsifie + "." + parties[2]));
    }

    @Test
    void unJetonSigneAvecUneAutreCleEstRefuse() {
        JwtService autreServeur = new JwtService("une-autre-cle-secrete-de-test-longue-99999", Duration.ofHours(24), MAINTENANT);

        assertNull(service.verifier(autreServeur.creerJeton(42L)));
    }

    @Test
    void unJetonExpireEstRefuse() {
        String jeton = service.creerJeton(42L);
        JwtService vingtCinqHeuresPlusTard = new JwtService(CLE, Duration.ofHours(24),
                Clock.offset(MAINTENANT, Duration.ofHours(25)));

        assertNull(vingtCinqHeuresPlusTard.verifier(jeton));
    }

    @Test
    void unTexteQuelconqueEstRefuse() {
        assertNull(service.verifier(null));
        assertNull(service.verifier(""));
        assertNull(service.verifier("abc.def"));
        assertNull(service.verifier("a.b.c"));
    }

    @Test
    void uneCleTropCourteEstInterdite() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("courte", Duration.ofHours(1), MAINTENANT));
    }
}
