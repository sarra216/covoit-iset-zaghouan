package tn.iset.covoit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ConnexionRequest;
import tn.iset.covoit.dto.InscriptionRequest;
import tn.iset.covoit.dto.UtilisateurResponse;
import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/** User story #1 : S'inscrire / Se connecter (+ vérification d'email, domaines autorisés, limite d'essais). */
@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompteService compteService;
    private final LimiteurConnexion limiteur;
    private final boolean verificationEmail;
    private final List<String> domainesAutorises;

    public AuthService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder,
                       CompteService compteService, LimiteurConnexion limiteur,
                       @Value("${app.verification-email:false}") boolean verificationEmail,
                       @Value("${app.inscription.domaines:}") String domaines) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.compteService = compteService;
        this.limiteur = limiteur;
        this.verificationEmail = verificationEmail;
        this.domainesAutorises = Arrays.stream(domaines.split(","))
                .map(d -> d.trim().toLowerCase().replaceFirst("^@", ""))
                .filter(d -> !d.isEmpty())
                .toList();
    }

    public boolean verificationActive() {
        return verificationEmail;
    }

    @Transactional
    public UtilisateurResponse inscrire(InscriptionRequest requete) {
        String email = requete.email().trim().toLowerCase();
        verifierDomaine(email);
        if (requete.role() == Role.ADMIN) {
            throw ApiException.requeteInvalide("Rôle non autorisé");
        }
        if (utilisateurRepository.existsByEmail(email)) {
            throw ApiException.conflit("Un compte existe déjà avec cet email");
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(requete.nom().trim());
        utilisateur.setPrenom(requete.prenom().trim());
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(passwordEncoder.encode(requete.motDePasse()));
        utilisateur.setTelephone(requete.telephone());
        utilisateur.setRole(requete.role());
        utilisateur.setDateAcceptationConditions(LocalDateTime.now());
        utilisateur.setEmailVerifie(!verificationEmail);
        utilisateur = utilisateurRepository.save(utilisateur);
        LOG.info("Nouveau compte {} ({})", utilisateur.getId(), email);

        if (verificationEmail) {
            compteService.envoyerVerification(utilisateur);
        }
        return UtilisateurResponse.from(utilisateur);
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse connecter(ConnexionRequest requete) {
        String email = requete.email().trim().toLowerCase();
        limiteur.verifier(email);
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(requete.motDePasse(), u.getMotDePasse()))
                .orElse(null);
        if (utilisateur == null) {
            limiteur.echec(email);
            LOG.warn("Connexion refusée pour {}", email);
            // Même message dans les deux cas : on ne révèle pas si l'email existe
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
        }
        limiteur.reussite(email);
        if (utilisateur.isSuspendu()) {
            throw ApiException.interdit("Ce compte est suspendu. Contactez l'administration de l'ISET.");
        }
        if (verificationEmail && !utilisateur.isEmailVerifie()) {
            throw ApiException.interdit("Confirmez d'abord votre email : cliquez sur le lien reçu à l'inscription.");
        }
        return UtilisateurResponse.from(utilisateur);
    }

    private void verifierDomaine(String email) {
        if (domainesAutorises.isEmpty()) return;
        String domaine = email.substring(email.indexOf('@') + 1);
        if (!domainesAutorises.contains(domaine)) {
            throw ApiException.requeteInvalide("Utilisez votre email de l'institut (@" + domainesAutorises.get(0) + ")");
        }
    }
}
