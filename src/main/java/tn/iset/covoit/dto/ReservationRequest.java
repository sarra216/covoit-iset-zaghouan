package tn.iset.covoit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull(message = "Le trajet est obligatoire") Long trajetId,
        @NotNull(message = "Le passager est obligatoire") Long passagerId,
        @Min(value = 1, message = "Il faut réserver au moins 1 place") int nombrePlaces
) {
}
