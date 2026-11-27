package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        TrajetResponse trajet,
        Long passagerId,
        String passagerNom,
        int nombrePlaces,
        StatutReservation statut,
        LocalDateTime dateReservation,
        boolean avisDonne
) {

    public static ReservationResponse from(Reservation r) {
        return from(r, false);
    }

    public static ReservationResponse from(Reservation r, boolean avisDonne) {
        return new ReservationResponse(r.getId(), TrajetResponse.from(r.getTrajet()), r.getPassager().getId(),
                r.getPassager().getNomComplet(), r.getNombrePlaces(), r.getStatut(), r.getDateReservation(), avisDonne);
    }
}
