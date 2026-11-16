package tn.iset.covoit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssistantRequest(
        @NotNull(message = "L'utilisateur est obligatoire") Long utilisateurId,
        @NotBlank(message = "Le message est vide")
        @Size(max = 300, message = "300 caractères maximum") String message
) {
}
