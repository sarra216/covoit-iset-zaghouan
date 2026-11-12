package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Trajet;

import java.time.LocalDate;
import java.time.LocalTime;

public record TrajetResponse(
        Long id,
        String villeDepart,
        String villeArrivee,
        LocalDate dateDepart,
        LocalTime heureDepart,
        int placesTotales,
        int placesDisponibles,
        double prix,
        Long conducteurId,
        String conducteurNom
) {

    public static TrajetResponse from(Trajet t) {
        return new TrajetResponse(t.getId(), t.getVilleDepart(), t.getVilleArrivee(), t.getDateDepart(),
                t.getHeureDepart(), t.getPlacesTotales(), t.getPlacesDisponibles(), t.getPrix(),
                t.getConducteur().getId(), t.getConducteur().getNomComplet());
    }
}
