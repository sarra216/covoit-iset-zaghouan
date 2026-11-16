package tn.iset.covoit.dto;

import jakarta.validation.constraints.NotBlank;

/** On redemande le mot de passe avant de supprimer un compte. */
public record SuppressionCompteRequest(@NotBlank(message = "Saisissez votre mot de passe") String motDePasse) {
}
