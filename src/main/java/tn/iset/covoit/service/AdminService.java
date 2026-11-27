package tn.iset.covoit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.AdminStatistiques;
import tn.iset.covoit.dto.AdminUtilisateur;
import tn.iset.covoit.dto.SignalementResponse;
import tn.iset.covoit.entity.Signalement;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;
import tn.iset.covoit.repository.SignalementRepository;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Tableau de bord de l'administrateur : statistiques, comptes, signalements. */
@Service
public class AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminService.class);

    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;
    private final SignalementRepository signalementRepository;

    public AdminService(UtilisateurRepository utilisateurRepository, TrajetRepository trajetRepository,
                        ReservationRepository reservationRepository, SignalementRepository signalementRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
        this.signalementRepository = signalementRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatistiques statistiques() {
        return new AdminStatistiques(
                utilisateurRepository.count(),
                trajetRepository.countByDateDepartGreaterThanEqual(LocalDate.now()),
                reservationRepository.countByStatutIn(List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE)),
                signalementRepository.countByTraiteFalse());
    }

    @Transactional(readOnly = true)
    public List<AdminUtilisateur> utilisateurs(String recherche) {
        String q = recherche == null ? "" : recherche.trim();
        return utilisateurRepository.rechercher(q).stream()
                .limit(100)
                .map(u -> AdminUtilisateur.from(u, signalementRepository.countBySignaleId(u.getId())))
                .toList();
    }

    @Transactional
    public AdminUtilisateur suspendre(Long id, boolean suspendu, Long adminId) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> ApiException.introuvable("Utilisateur introuvable"));
        if (u.getId().equals(adminId) || u.estAdmin()) {
            throw ApiException.requeteInvalide("Un compte administrateur ne peut pas être suspendu ici");
        }
        if (u.isSupprime()) {
            throw ApiException.requeteInvalide("Ce compte a été supprimé");
        }
        u.setSuspendu(suspendu);
        LOG.warn("Admin {} : compte {} {}", adminId, id, suspendu ? "suspendu" : "réactivé");
        return AdminUtilisateur.from(u, signalementRepository.countBySignaleId(id));
    }

    @Transactional(readOnly = true)
    public List<SignalementResponse> signalements(boolean traites) {
        return signalementRepository.findByTraiteOrderByDateCreationDesc(traites).stream()
                .map(s -> SignalementResponse.from(s, signalementRepository.countBySignaleId(s.getSignale().getId())))
                .toList();
    }

    @Transactional
    public SignalementResponse traiter(Long id, Long adminId) {
        Signalement s = signalementRepository.findById(id)
                .orElseThrow(() -> ApiException.introuvable("Signalement introuvable"));
        s.setTraite(true);
        s.setDateTraitement(LocalDateTime.now());
        LOG.info("Admin {} : signalement {} traité", adminId, id);
        return SignalementResponse.from(s, signalementRepository.countBySignaleId(s.getSignale().getId()));
    }
}
