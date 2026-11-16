package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.Utilisateur;

import java.time.LocalDateTime;

/** Vue d'un compte pour l'administration (avec l'email, jamais le mot de passe). */
public record AdminUtilisateur(Long id, String nomComplet, String email, Role role, LocalDateTime dateInscription,
                               boolean emailVerifie, boolean suspendu, boolean supprime, long signalements) {

    public static AdminUtilisateur from(Utilisateur u, long signalements) {
        return new AdminUtilisateur(u.getId(), u.getNomComplet(), u.getEmail(), u.getRole(), u.getDateInscription(),
                u.isEmailVerifie(), u.isSuspendu(), u.isSupprime(), signalements);
    }
}
