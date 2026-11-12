package tn.iset.covoit.dto;

import java.time.LocalDate;

/** Profil public d'un passager : pas d'email ni de téléphone (vie privée). */
public record ProfilPassagerResponse(
        Long id,
        String nom,
        String prenom,
        LocalDate membreDepuis,
        long reservationsConfirmees,
        long reservationsAnnulees
) {
}
