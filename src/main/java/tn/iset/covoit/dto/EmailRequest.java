package tn.iset.covoit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Mot de passe oublié, ou renvoi de l'email de vérification. */
public record EmailRequest(@NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email) {
}
