package tn.iset.covoit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.UtilisateurResponse;
import tn.iset.covoit.entity.*;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Vérification de l'email, mot de passe oublié et suppression du compte. */
@Service
public class CompteService {

    private static final Logger LOG = LoggerFactory.getLogger(CompteService.class);
    private static final Duration VALIDITE_VERIFICATION = Duration.ofHours(48);
    private static final Duration VALIDITE_REINITIALISATION = Duration.ofHours(1);
    private static final List<StatutReservation> ACTIVES = List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE);

    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;
    private final JetonEmailService jetonEmailService;
    private final EmailService emailService;
    private final AnnulationTrajetService annulationTrajetService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final String urlApplication;

    public CompteService(UtilisateurRepository utilisateurRepository, TrajetRepository trajetRepository,
                         ReservationRepository reservationRepository, JetonEmailService jetonEmailService,
                         EmailService emailService, AnnulationTrajetService annulationTrajetService,
                         NotificationService notificationService, PasswordEncoder passwordEncoder,
                         @Value("${app.url:http://localhost:8082}") String urlApplication) {
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
        this.jetonEmailService = jetonEmailService;
        this.emailService = emailService;
        this.annulationTrajetService = annulationTrajetService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.urlApplication = urlApplication.replaceAll("/+$", "");
    }

    // ---------- Vérification de l'email ----------

    @Transactional
    public void envoyerVerification(Utilisateur u) {
        String jeton = jetonEmailService.creer(u, TypeJeton.VERIFICATION_EMAIL, VALIDITE_VERIFICATION);
        emailService.envoyer(u.getEmail(), "Confirmez votre compte Covoit ISET Zaghouan",
                "Bonjour " + u.getPrenom() + ",\n\n"
                        + "Bienvenue sur Covoit ISET Zaghouan ! Pour activer votre compte, ouvrez ce lien :\n"
                        + urlApplication + "/index.html?verifier=" + jeton + "\n\n"
                        + "Ce lien est valable 48 heures. Si vous n'avez pas créé de compte, ignorez cet email.\n\n"
                        + "L'équipe Covoit ISET Zaghouan");
    }

    /** Renvoie l'email de vérification, sans révéler si l'adresse existe. */
    @Transactional
    public void renvoyerVerification(String email) {
        utilisateurRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> !u.isEmailVerifie() && !u.isSupprime())
                .ifPresent(this::envoyerVerification);
    }

    @Transactional
    public UtilisateurResponse verifierEmail(String jeton) {
        Utilisateur u = jetonEmailService.consommer(jeton, TypeJeton.VERIFICATION_EMAIL);
        u.setEmailVerifie(true);
        LOG.info("Email vérifié pour le compte {}", u.getId());
        return UtilisateurResponse.from(u);
    }

    // ---------- Mot de passe oublié ----------

    /** Toujours la même réponse, que l'email existe ou non. */
    @Transactional
    public void motDePasseOublie(String email) {
        utilisateurRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> !u.isSupprime())
                .ifPresent(u -> {
                    String jeton = jetonEmailService.creer(u, TypeJeton.REINITIALISATION_MOT_DE_PASSE, VALIDITE_REINITIALISATION);
                    emailService.envoyer(u.getEmail(), "Nouveau mot de passe · Covoit ISET Zaghouan",
                            "Bonjour " + u.getPrenom() + ",\n\n"
                                    + "Pour choisir un nouveau mot de passe, ouvrez ce lien :\n"
                                    + urlApplication + "/reinitialiser.html?jeton=" + jeton + "\n\n"
                                    + "Ce lien est valable 1 heure et ne fonctionne qu'une fois. "
                                    + "Si vous n'avez rien demandé, ignorez cet email : votre mot de passe ne change pas.\n\n"
                                    + "L'équipe Covoit ISET Zaghouan");
                });
    }

    @Transactional
    public void reinitialiser(String jeton, String nouveauMotDePasse) {
        Utilisateur u = jetonEmailService.consommer(jeton, TypeJeton.REINITIALISATION_MOT_DE_PASSE);
        u.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        u.setEmailVerifie(true); // il a prouvé qu'il reçoit les emails de cette adresse
        LOG.info("Mot de passe réinitialisé pour le compte {}", u.getId());
    }

    // ---------- Suppression du compte ----------

    /**
     * On ne supprime pas la ligne (les trajets, messages et avis des autres y font référence) :
     * on efface les données personnelles et on ferme le compte.
     */
    @Transactional
    public void supprimer(Long id, String motDePasse) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> ApiException.introuvable("Utilisateur introuvable"));
        if (!passwordEncoder.matches(motDePasse, u.getMotDePasse())) {
            throw ApiException.interdit("Mot de passe incorrect");
        }

        // 1. Ses trajets à venir sont annulés (les passagers sont prévenus)
        trajetRepository.findByConducteurIdAndDateDepartGreaterThanEqualOrderByDateDepartAscHeureDepartAsc(id, LocalDate.now())
                .stream()
                .filter(t -> !t.isAnnule() && !t.estPasse())
                .forEach(annulationTrajetService::annulerSansControle);

        // 2. Ses réservations à venir sont annulées (les places sont rendues, les conducteurs prévenus)
        for (Reservation r : reservationRepository.findByPassagerIdAndStatutIn(id, ACTIVES)) {
            Trajet t = r.getTrajet();
            if (t.estPasse()) continue;
            r.setStatut(StatutReservation.ANNULEE);
            r.setDateAnnulation(LocalDateTime.now());
            t.setPlacesDisponibles(t.getPlacesDisponibles() + r.getNombrePlaces());
            notificationService.notifier(t.getConducteur(), TypeNotification.RESERVATION_ANNULEE,
                    "Réservation annulée",
                    "Un passager a supprimé son compte : " + r.getNombrePlaces() + " place(s) libérée(s) sur "
                            + t.getVilleDepart() + " → " + t.getVilleArrivee() + " le " + t.getDateDepart() + ".",
                    "trajet.html?id=" + t.getId());
        }

        // 3. Données personnelles effacées
        u.setPrenom("Compte");
        u.setNom("supprimé");
        u.setEmail("supprime-" + id + "@compte.invalid");
        u.setTelephone("00000000");
        u.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
        u.setVoitureModele(null);
        u.setVoitureCouleur(null);
        u.setMatricule(null);
        u.setSupprime(true);
        u.setSuspendu(true); // son jeton JWT n'est plus accepté
        LOG.info("Compte {} supprimé à la demande de l'utilisateur", id);
    }
}
