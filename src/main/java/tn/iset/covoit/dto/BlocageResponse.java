package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Blocage;

import java.time.LocalDateTime;

public record BlocageResponse(Long utilisateurId, String nom, LocalDateTime depuis) {

    public static BlocageResponse from(Blocage b) {
        return new BlocageResponse(b.getBloque().getId(), b.getBloque().getNomComplet(), b.getDateCreation());
    }
}
