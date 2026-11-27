package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Avis;

import java.time.LocalDateTime;

public record AvisResponse(Long id, int note, String commentaire, Long passagerId, String passagerNom,
                           String trajet, LocalDateTime dateCreation) {

    public static AvisResponse from(Avis a) {
        return new AvisResponse(a.getId(), a.getNote(), a.getCommentaire(), a.getPassager().getId(),
                a.getPassager().getNomComplet(),
                a.getReservation().getTrajet().getVilleDepart() + " → " + a.getReservation().getTrajet().getVilleArrivee(),
                a.getDateCreation());
    }
}
