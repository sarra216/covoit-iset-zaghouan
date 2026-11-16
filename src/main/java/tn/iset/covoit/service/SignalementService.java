package tn.iset.covoit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.SignalementRequest;
import tn.iset.covoit.entity.*;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.SignalementRepository;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

/** Un utilisateur signale quelqu'un ; les administrateurs sont prévenus. */
@Service
public class SignalementService {

    private static final Logger LOG = LoggerFactory.getLogger(SignalementService.class);

    private final SignalementRepository signalementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;
    private final NotificationService notificationService;

    public SignalementService(SignalementRepository signalementRepository, UtilisateurRepository utilisateurRepository,
                              TrajetRepository trajetRepository, NotificationService notificationService) {
        this.signalementRepository = signalementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void signaler(SignalementRequest requete) {
        if (requete.auteurId().equals(requete.signaleId())) {
            throw ApiException.requeteInvalide("Vous ne pouvez pas vous signaler vous-même");
        }
        Utilisateur auteur = utilisateurRepository.findById(requete.auteurId())
                .orElseThrow(() -> ApiException.introuvable("Utilisateur introuvable"));
        Utilisateur signale = utilisateurRepository.findById(requete.signaleId())
                .orElseThrow(() -> ApiException.introuvable("Utilisateur introuvable"));
        if (signalementRepository.existsByAuteurIdAndSignaleIdAndTraiteFalse(auteur.getId(), signale.getId())) {
            throw ApiException.conflit("Vous avez déjà signalé cette personne ; l'administration va l'examiner");
        }

        Signalement s = new Signalement();
        s.setAuteur(auteur);
        s.setSignale(signale);
        s.setMotif(requete.motif());
        if (requete.details() != null && !requete.details().isBlank()) s.setDetails(requete.details().trim());
        if (requete.trajetId() != null) {
            s.setTrajet(trajetRepository.findById(requete.trajetId()).orElse(null));
        }
        signalementRepository.save(s);
        LOG.warn("Signalement de {} contre {} ({})", auteur.getId(), signale.getId(), requete.motif());

        for (Utilisateur admin : utilisateurRepository.findByRole(Role.ADMIN)) {
            notificationService.notifier(admin, TypeNotification.NOUVEAU_SIGNALEMENT, "Nouveau signalement",
                    signale.getNomComplet() + " a été signalé(e) par " + auteur.getNomComplet() + ".", "admin.html");
        }
    }
}
