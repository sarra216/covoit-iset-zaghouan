package tn.iset.covoit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tn.iset.covoit.entity.Role;

public record InscriptionRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "Le prénom est obligatoire") String prenom,
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères") String motDePasse,
        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "\\d{8}", message = "Le téléphone doit contenir 8 chiffres") String telephone,
        @NotNull(message = "Le rôle est obligatoire (CONDUCTEUR ou PASSAGER)") Role role
) {
}
