package tn.iset.covoit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Formulaire « Compléter le profil » : identité + mode conducteur + véhicule. */
public record ProfilRequest(
        @NotBlank(message = "Le prénom est obligatoire") String prenom,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "\\d{8}", message = "Le téléphone doit contenir 8 chiffres") String telephone,
        boolean conducteur,
        @Size(max = 60, message = "Modèle trop long") String voitureModele,
        @Size(max = 30, message = "Couleur trop longue") String voitureCouleur,
        @Size(max = 20, message = "Matricule trop long") String matricule
) {
}
