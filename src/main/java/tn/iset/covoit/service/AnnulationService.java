package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;

import java.time.LocalDateTime;

/** User story #6 : Annuler une réservation */
@Service
public class AnnulationService {

    private final ReservationRepository reservationRepository;

    public AnnulationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse annuler(Long reservationId, Long passagerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.introuvable("Réservation introuvable"));

        if (!reservation.getPassager().getId().equals(passagerId)) {
            throw ApiException.interdit("Cette réservation ne vous appartient pas");
        }
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw ApiException.conflit("Cette réservation est déjà annulée");
        }

        Trajet trajet = reservation.getTrajet();
        if (trajet.estPasse()) {
            throw ApiException.requeteInvalide("Impossible d'annuler : le trajet est déjà parti");
        }

        // Les places sont rendues au trajet
        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() + reservation.getNombrePlaces());
        reservation.setStatut(StatutReservation.ANNULEE);
        reservation.setDateAnnulation(LocalDateTime.now());

        return ReservationResponse.from(reservation);
    }
}
