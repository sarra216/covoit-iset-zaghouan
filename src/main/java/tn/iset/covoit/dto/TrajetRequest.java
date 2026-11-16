package tn.iset.covoit.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record TrajetRequest(
        @NotNull(message = "Le conducteur est obligatoire") Long conducteurId,
        @NotBlank(message = "La ville de départ est obligatoire") String villeDepart,
        @NotBlank(message = "La ville d'arrivée est obligatoire") String villeArrivee,
        @NotNull(message = "La date est obligatoire")
        @FutureOrPresent(message = "La date ne peut pas être dans le passé") LocalDate dateDepart,
        @NotNull(message = "L'heure est obligatoire") LocalTime heureDepart,
        @Min(value = 1, message = "Il faut au moins 1 place")
        @Max(value = 8, message = "8 places maximum") int places,
        @PositiveOrZero(message = "Le prix ne peut pas être négatif") double prix,
        @Size(max = 200, message = "Lieu de rendez-vous trop long") String lieuDepart,
        Double latitude,
        Double longitude
) {
}
