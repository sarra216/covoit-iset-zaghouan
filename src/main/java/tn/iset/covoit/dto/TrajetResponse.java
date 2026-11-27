package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.entity.Utilisateur;

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
        String lieuDepart,
        Double latitude,
        Double longitude,
        Long conducteurId,
        String conducteurNom,
        String voitureModele,
        String voitureCouleur,
        Double noteConducteur,
        int nombreAvis,
        boolean annule
) {

    public static TrajetResponse from(Trajet t) {
        Utilisateur c = t.getConducteur();
        return new TrajetResponse(t.getId(), t.getVilleDepart(), t.getVilleArrivee(), t.getDateDepart(),
                t.getHeureDepart(), t.getPlacesTotales(), t.getPlacesDisponibles(), t.getPrix(),
                t.getLieuDepart(), t.getLatitude(), t.getLongitude(),
                c.getId(), c.getNomComplet(), c.getVoitureModele(), c.getVoitureCouleur(),
                c.getNoteMoyenne(), c.getNombreAvis(), t.isAnnule());
    }
}
