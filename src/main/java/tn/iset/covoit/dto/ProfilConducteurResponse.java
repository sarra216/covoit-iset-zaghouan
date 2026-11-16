package tn.iset.covoit.dto;

import java.time.LocalDate;
import java.util.List;

/** Profil public d'un conducteur : pas d'email ni de téléphone (vie privée). */
public record ProfilConducteurResponse(
        Long id,
        String nom,
        String prenom,
        LocalDate membreDepuis,
        long nombreTrajets,
        String voitureModele,
        String voitureCouleur,
        Double noteMoyenne,
        int nombreAvis,
        List<TrajetResponse> trajetsAVenir
) {
}
