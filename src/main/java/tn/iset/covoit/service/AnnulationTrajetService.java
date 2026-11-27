package tn.iset.covoit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.TrajetResponse;
import tn.iset.covoit.entity.*;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;
import tn.iset.covoit.repository.TrajetRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Le conducteur annule un trajet qu'il a publié : toutes les réservations sont fermées et les passagers prévenus. */
@Service
public class AnnulationTrajetService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnulationTrajetService.class);
    private static final List<StatutReservation> ACTIVES = List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE);

    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public AnnulationTrajetService(TrajetRepository trajetRepository, ReservationRepository reservationRepository,
                                   NotificationService notificationService) {
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public TrajetResponse annuler(Long trajetId, Long conducteurId) {
        Trajet t = trajetRepository.findById(trajetId)
                .orElseThrow(() -> ApiException.introuvable("Trajet introuvable"));
        if (!t.getConducteur().getId().equals(conducteurId)) {
            throw ApiException.interdit("Seul le conducteur peut annuler ce trajet");
        }
        if (t.isAnnule()) {
            throw ApiException.conflit("Ce trajet est déjà annulé");
        }
        if (t.estPasse()) {
            throw ApiException.requeteInvalide("Ce trajet est déjà parti");
        }
        return TrajetResponse.from(annulerSansControle(t));
    }

    /** Utilisé aussi lors de la suppression d'un compte. */
    @Transactional
    public Trajet annulerSansControle(Trajet t) {
        t.setAnnule(true);
        int prevenus = 0;
        for (Reservation r : reservationRepository.findByTrajetIdAndStatutInOrderByDateReservation(t.getId(), ACTIVES)) {
            r.setStatut(StatutReservation.ANNULEE);
            r.setDateAnnulation(LocalDateTime.now());
            notificationService.notifier(r.getPassager(), TypeNotification.TRAJET_ANNULE,
                    "Trajet annulé par le conducteur",
                    t.getConducteur().getNomComplet() + " a annulé le trajet " + t.getVilleDepart() + " → "
                            + t.getVilleArrivee() + " du " + t.getDateDepart() + " à " + t.getHeureDepart()
                            + ". Cherchez un autre trajet.",
                    "recherche.html");
            prevenus++;
        }
        LOG.info("Trajet {} annulé, {} passager(s) prévenu(s)", t.getId(), prevenus);
        return t;
    }
}
