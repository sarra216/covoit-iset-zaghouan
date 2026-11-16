package tn.iset.covoit.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crée et vérifie les jetons JWT (JSON Web Token) signés en HMAC-SHA256.
 *
 * Un jeton a trois parties séparées par des points : en-tête.contenu.signature
 *  - en-tête  : {"alg":"HS256","typ":"JWT"}
 *  - contenu  : {"sub":"<id utilisateur>","iat":<émis à>,"exp":<expire à>}
 *  - signature: HMAC-SHA256(en-tête.contenu, clé secrète du serveur)
 * Sans la clé secrète, personne ne peut fabriquer ni modifier un jeton valide.
 */
@Service
public class JwtService {

    private static final Base64.Encoder ENCODEUR = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODEUR = Base64.getUrlDecoder();
    private static final String EN_TETE = ENCODEUR.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final Pattern SUJET = Pattern.compile("\"sub\":\"(\\d+)\"");
    private static final Pattern EXPIRATION = Pattern.compile("\"exp\":(\\d+)");

    private final byte[] cle;
    private final Duration duree;
    private final Clock horloge;

    @Autowired
    public JwtService(@Value("${app.jwt.secret:covoit-iset-zaghouan-cle-de-developpement-a-changer-en-production}") String secret,
                      @Value("${app.jwt.duree-heures:24}") long dureeHeures) {
        this(secret, Duration.ofHours(dureeHeures), Clock.systemUTC());
    }

    /** Constructeur utilisé par les tests (horloge contrôlée). */
    JwtService(String secret, Duration duree, Clock horloge) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("app.jwt.secret doit contenir au moins 32 caractères");
        }
        this.cle = secret.getBytes(StandardCharsets.UTF_8);
        this.duree = duree;
        this.horloge = horloge;
    }

    /** Fabrique un jeton pour l'utilisateur, valable pendant la durée configurée. */
    public String creerJeton(Long utilisateurId) {
        long maintenant = horloge.instant().getEpochSecond();
        String contenu = "{\"sub\":\"" + utilisateurId + "\",\"iat\":" + maintenant
                + ",\"exp\":" + (maintenant + duree.getSeconds()) + "}";
        String debut = EN_TETE + "." + ENCODEUR.encodeToString(contenu.getBytes(StandardCharsets.UTF_8));
        return debut + "." + signer(debut);
    }

    /** Renvoie l'id de l'utilisateur si le jeton est intact et non expiré, sinon null. */
    public Long verifier(String jeton) {
        if (jeton == null) return null;
        String[] parties = jeton.split("\\.");
        if (parties.length != 3 || !parties[0].equals(EN_TETE)) return null;

        // Comparaison en temps constant : ne révèle pas où la signature diffère
        byte[] attendue = signer(parties[0] + "." + parties[1]).getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(attendue, parties[2].getBytes(StandardCharsets.US_ASCII))) return null;

        String contenu;
        try {
            contenu = new String(DECODEUR.decode(parties[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
        Matcher sujet = SUJET.matcher(contenu);
        Matcher exp = EXPIRATION.matcher(contenu);
        if (!sujet.find() || !exp.find()) return null;
        if (Long.parseLong(exp.group(1)) <= horloge.instant().getEpochSecond()) return null;
        return Long.parseLong(sujet.group(1));
    }

    private String signer(String donnees) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(cle, "HmacSHA256"));
            return ENCODEUR.encodeToString(mac.doFinal(donnees.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Signature JWT impossible", e);
        }
    }
}
