package tn.iset.covoit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @NotNull(message = "L'expéditeur est obligatoire") Long expediteurId,
        @NotNull(message = "Le destinataire est obligatoire") Long destinataireId,
        Long trajetId,
        @NotBlank(message = "Le message est vide")
        @Size(max = 500, message = "500 caractères maximum") String contenu
) {
}
