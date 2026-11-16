package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import tn.iset.covoit.entity.JetonEmail;
import tn.iset.covoit.entity.TypeJeton;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.JetonEmailRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/** Crée et consomme les liens à usage unique envoyés par email. */
@Service
public class JetonEmailService {

    private static final SecureRandom HASARD = new SecureRandom();

    private final JetonEmailRepository jetonEmailRepository;

    public JetonEmailService(JetonEmailRepository jetonEmailRepository) {
        this.jetonEmailRepository = jetonEmailRepository;
    }

    /** Renvoie le jeton en clair (à mettre dans le lien) ; seule son empreinte est enregistrée. */
    public String creer(Utilisateur utilisateur, TypeJeton type, Duration validite) {
        byte[] octets = new byte[32];
        HASARD.nextBytes(octets);
        String jeton = Base64.getUrlEncoder().withoutPadding().encodeToString(octets);

        JetonEmail j = new JetonEmail();
        j.setUtilisateur(utilisateur);
        j.setType(type);
        j.setEmpreinte(empreinte(jeton));
        j.setExpiration(LocalDateTime.now().plus(validite));
        jetonEmailRepository.save(j);
        return jeton;
    }

    /** Vérifie le jeton, le marque comme utilisé et renvoie son propriétaire. */
    public Utilisateur consommer(String jeton, TypeJeton type) {
        JetonEmail j = jetonEmailRepository.findByEmpreinte(empreinte(jeton == null ? "" : jeton))
                .filter(x -> x.getType() == type)
                .orElseThrow(() -> ApiException.requeteInvalide("Ce lien n'est pas valide"));
        if (j.isUtilise()) {
            throw ApiException.requeteInvalide("Ce lien a déjà été utilisé");
        }
        if (j.getExpiration().isBefore(LocalDateTime.now())) {
            throw ApiException.requeteInvalide("Ce lien a expiré. Demandez-en un nouveau.");
        }
        j.setUtilise(true);
        return j.getUtilisateur();
    }

    static String empreinte(String jeton) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(jeton.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
