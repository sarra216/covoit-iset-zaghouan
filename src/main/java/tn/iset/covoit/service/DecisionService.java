package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.entity.TypeNotification;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Le conducteur accepte ou refuse les demandes de réservation. */
@Service
public class DecisionService {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public DecisionService(ReservationRepository reservationRepository, NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    /** Toutes les demandes en attente sur les trajets du conducteur. */
    @Transactional(readOnly = true)
    public List<ReservationResponse> demandesEnAttente(Long conducteurId) {
        return reservationRepository
                .findByTrajetConducteurIdAndStatutOrderByDateReservation(conducteurId, StatutReservation.EN_ATTENTE)
                .stream()
                .filter(r -> !r.getTrajet().estPasse())
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse accepter(Long reservationId, Long conducteurId) {
        Reservation r = demandeDuConducteur(reservationId, conducteurId);
        r.setStatut(StatutReservation.CONFIRMEE);
        r.setDateDecision(LocalDateTime.now());

        Trajet t = r.getTrajet();
        notificationService.notifier(r.getPassager(), TypeNotification.RESERVATION_ACCEPTEE,
                "Réservation acceptée",
                t.getConducteur().getNomComplet() + " a accepté votre demande pour " + t.getVilleDepart() + " → "
                        + t.getVilleArrivee() + " le " + t.getDateDepart() + " à " + t.getHeureDepart() + ".",
                "trajet.html?id=" + t.getId());
        return ReservationResponse.from(r);
    }

    @Transactional
    public ReservationResponse refuser(Long reservationId, Long conducteurId) {
        Reservation r = demandeDuConducteur(reservationId, conducteurId);
        r.setStatut(StatutReservation.REFUSEE);
        r.setDateDecision(LocalDateTime.now());

        // Les places bloquées par la demande sont rendues au trajet
        Trajet t = r.getTrajet();
        t.setPlacesDisponibles(t.getPlacesDisponibles() + r.getNombrePlaces());

        notificationService.notifier(r.getPassager(), TypeNotification.RESERVATION_REFUSEE,
                "Demande refusée",
                t.getConducteur().getNomComplet() + " ne peut pas vous prendre pour " + t.getVilleDepart() + " → "
                        + t.getVilleArrivee() + " le " + t.getDateDepart() + ". Cherchez un autre trajet.",
                "recherche.html");
        return ReservationResponse.from(r);
    }

    private Reservation demandeDuConducteur(Long reservationId, Long conducteurId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.introuvable("Demande introuvable"));
        if (!r.getTrajet().getConducteur().getId().equals(conducteurId)) {
            throw ApiException.interdit("Seul le conducteur du trajet peut répondre à cette demande");
        }
        if (r.getStatut() != StatutReservation.EN_ATTENTE) {
            throw ApiException.conflit("Cette demande a déjà reçu une réponse");
        }
        if (r.getTrajet().estPasse()) {
            throw ApiException.requeteInvalide("Le trajet est déjà parti");
        }
        return r;
    }
}
