package tn.iset.covoit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.iset.covoit.entity.MotifSignalement;

public record SignalementRequest(
        @NotNull(message = "L'auteur est obligatoire") Long auteurId,
        @NotNull(message = "La personne signalée est obligatoire") Long signaleId,
        Long trajetId,
        @NotNull(message = "Choisissez un motif") MotifSignalement motif,
        @Size(max = 1000, message = "1000 caractères maximum") String details
) {
}
