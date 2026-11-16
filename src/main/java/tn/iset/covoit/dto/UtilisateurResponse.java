package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.Utilisateur;

/** Données de l'utilisateur connecté (sans le mot de passe). */
public record UtilisateurResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        Role role,
        String voitureModele,
        String voitureCouleur,
        String matricule
) {

    public static UtilisateurResponse from(Utilisateur u) {
        return new UtilisateurResponse(u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getTelephone(),
                u.getRole(), u.getVoitureModele(), u.getVoitureCouleur(), u.getMatricule());
    }
}
