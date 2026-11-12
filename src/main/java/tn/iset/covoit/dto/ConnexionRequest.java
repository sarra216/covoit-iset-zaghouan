package tn.iset.covoit.dto;

import jakarta.validation.constraints.NotBlank;

public record ConnexionRequest(
        @NotBlank(message = "L'email est obligatoire") String email,
        @NotBlank(message = "Le mot de passe est obligatoire") String motDePasse
) {
}
