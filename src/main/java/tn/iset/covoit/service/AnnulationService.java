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

/** User story #6 : Annuler une réservation (en attente ou confirmée). */
@Service
public class AnnulationService {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public AnnulationService(ReservationRepository reservationRepository, NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReservationResponse annuler(Long reservationId, Long passagerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.introuvable("Réservation introuvable"));

        if (!reservation.getPassager().getId().equals(passagerId)) {
            throw ApiException.interdit("Cette réservation ne vous appartient pas");
        }
        if (!reservation.estActive()) {
            throw ApiException.conflit("Cette réservation est déjà annulée ou refusée");
        }

        Trajet trajet = reservation.getTrajet();
        if (trajet.estPasse()) {
            throw ApiException.requeteInvalide("Impossible d'annuler : le trajet est déjà parti");
        }

        // Les places sont rendues au trajet
        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() + reservation.getNombrePlaces());
        reservation.setStatut(StatutReservation.ANNULEE);
        reservation.setDateAnnulation(LocalDateTime.now());

        notificationService.notifier(trajet.getConducteur(), TypeNotification.RESERVATION_ANNULEE,
                "Réservation annulée",
                reservation.getPassager().getNomComplet() + " a annulé sa réservation pour " + trajet.getVilleDepart()
                        + " → " + trajet.getVilleArrivee() + " le " + trajet.getDateDepart() + ". "
                        + reservation.getNombrePlaces() + " place(s) libérée(s).",
                "trajet.html?id=" + trajet.getId());

        return ReservationResponse.from(reservation);
    }
}
