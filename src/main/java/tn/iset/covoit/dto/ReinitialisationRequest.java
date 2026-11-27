package tn.iset.covoit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReinitialisationRequest(
        @NotBlank(message = "Lien invalide") String jeton,
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères") String motDePasse
) {
}
