package tn.iset.covoit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvisRequest(
        @NotNull(message = "La réservation est obligatoire") Long reservationId,
        @NotNull(message = "Le passager est obligatoire") Long passagerId,
        @Min(value = 1, message = "La note va de 1 à 5 étoiles")
        @Max(value = 5, message = "La note va de 1 à 5 étoiles") int note,
        @Size(max = 500, message = "500 caractères maximum") String commentaire
) {
}
