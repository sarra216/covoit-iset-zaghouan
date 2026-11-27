package tn.iset.covoit.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.iset.covoit.exception.ApiException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LimiteurConnexionTest {

    static final Instant DEBUT = Instant.parse("2026-11-14T10:00:00Z");

    @Test
    void apresCinqEchecsLaConnexionEstBloquee() {
        LimiteurConnexion limiteur = new LimiteurConnexion(Clock.fixed(DEBUT, ZoneOffset.UTC));
        for (int i = 0; i < 4; i++) limiteur.echec("sara@iset.tn");
        assertDoesNotThrow(() -> limiteur.verifier("sara@iset.tn"));

        limiteur.echec("sara@iset.tn");

        ApiException e = assertThrows(ApiException.class, () -> limiteur.verifier("sara@iset.tn"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatus());
        assertDoesNotThrow(() -> limiteur.verifier("autre@iset.tn"), "les autres comptes ne sont pas touchés");
    }

    @Test
    void leBlocageEstImmediatApresLeCinquiemeEchec() {
        LimiteurConnexion avant = new LimiteurConnexion(Clock.fixed(DEBUT, ZoneOffset.UTC));
        for (int i = 0; i < 5; i++) avant.echec("sara@iset.tn");
        assertThrows(ApiException.class, () -> avant.verifier("sara@iset.tn"));
    }

    @Test
    void uneConnexionReussieRemetLeCompteurAZero() {
        LimiteurConnexion limiteur = new LimiteurConnexion(Clock.fixed(DEBUT, ZoneOffset.UTC));
        for (int i = 0; i < 4; i++) limiteur.echec("sara@iset.tn");

        limiteur.reussite("sara@iset.tn");
        limiteur.echec("sara@iset.tn");

        assertDoesNotThrow(() -> limiteur.verifier("sara@iset.tn"));
    }

    @Test
    void lesEchecsAnciensSontOublies() {
        MutableClock horloge = new MutableClock(DEBUT);
        LimiteurConnexion limiteur = new LimiteurConnexion(horloge);
        for (int i = 0; i < 4; i++) limiteur.echec("sara@iset.tn");

        horloge.avancer(Duration.ofMinutes(16));
        limiteur.echec("sara@iset.tn");

        assertDoesNotThrow(() -> limiteur.verifier("sara@iset.tn"));
    }

    @Test
    void leBlocageExpire() {
        MutableClock horloge = new MutableClock(DEBUT);
        LimiteurConnexion limiteur = new LimiteurConnexion(horloge);
        for (int i = 0; i < 5; i++) limiteur.echec("sara@iset.tn");

        horloge.avancer(Duration.ofMinutes(16));

        assertDoesNotThrow(() -> limiteur.verifier("sara@iset.tn"));
    }

    /** Horloge qu'on peut avancer pendant le test. */
    static class MutableClock extends Clock {
        private Instant maintenant;
        MutableClock(Instant debut) { this.maintenant = debut; }
        void avancer(Duration d) { maintenant = maintenant.plus(d); }
        @Override public Instant instant() { return maintenant; }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }
}
