package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;

import java.time.LocalDateTime;

/** Un passager qui a demandé ou réservé un trajet (vu par le conducteur). */
public record PassagerTrajet(Long reservationId, Long passagerId, String passagerNom, int nombrePlaces,
                             StatutReservation statut, LocalDateTime dateReservation) {

    public static PassagerTrajet from(Reservation r) {
        return new PassagerTrajet(r.getId(), r.getPassager().getId(), r.getPassager().getNomComplet(),
                r.getNombrePlaces(), r.getStatut(), r.getDateReservation());
    }
}
