package tn.iset.covoit.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tn.iset.covoit.exception.ApiException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protection contre la devinette de mots de passe :
 * après 5 échecs en 15 minutes sur un même email, la connexion est bloquée 15 minutes.
 * (En mémoire : suffisant pour un seul serveur ; à mettre en base si l'application tourne sur plusieurs.)
 */
@Component
public class LimiteurConnexion {

    static final int ESSAIS_MAX = 5;
    static final Duration FENETRE = Duration.ofMinutes(15);
    static final Duration BLOCAGE = Duration.ofMinutes(15);

    private record Etat(int echecs, Instant premierEchec, Instant bloqueJusqua) { }

    private final Map<String, Etat> etats = new ConcurrentHashMap<>();
    private final Clock horloge;

    public LimiteurConnexion() {
        this(Clock.systemUTC());
    }

    LimiteurConnexion(Clock horloge) {
        this.horloge = horloge;
    }

    /** À appeler avant de vérifier le mot de passe. */
    public void verifier(String email) {
        Etat e = etats.get(email);
        Instant maintenant = horloge.instant();
        if (e != null && e.bloqueJusqua() != null && maintenant.isBefore(e.bloqueJusqua())) {
            long minutes = Math.max(1, Duration.between(maintenant, e.bloqueJusqua()).toMinutes() + 1);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Trop de tentatives. Réessayez dans " + minutes + " minute" + (minutes > 1 ? "s" : "") + ".");
        }
    }

    public void echec(String email) {
        Instant maintenant = horloge.instant();
        etats.compute(email, (cle, e) -> {
            if (e == null || maintenant.isAfter(e.premierEchec().plus(FENETRE))
                    || (e.bloqueJusqua() != null && !maintenant.isBefore(e.bloqueJusqua()))) {
                e = new Etat(0, maintenant, null);
            }
            int echecs = e.echecs() + 1;
            return new Etat(echecs, e.premierEchec(), echecs >= ESSAIS_MAX ? maintenant.plus(BLOCAGE) : null);
        });
    }

    public void reussite(String email) {
        etats.remove(email);
    }
}
