package tn.iset.covoit.dto;

import tn.iset.covoit.entity.MotifSignalement;
import tn.iset.covoit.entity.Signalement;

import java.time.LocalDateTime;

public record SignalementResponse(Long id, Long auteurId, String auteurNom, Long signaleId, String signaleNom,
                                  boolean signaleSuspendu, long signalementsContreLui, Long trajetId, String trajet,
                                  MotifSignalement motif, String details, LocalDateTime dateCreation, boolean traite) {

    public static SignalementResponse from(Signalement s, long signalementsContreLui) {
        return new SignalementResponse(s.getId(), s.getAuteur().getId(), s.getAuteur().getNomComplet(),
                s.getSignale().getId(), s.getSignale().getNomComplet(), s.getSignale().isSuspendu(), signalementsContreLui,
                s.getTrajet() == null ? null : s.getTrajet().getId(),
                s.getTrajet() == null ? null : s.getTrajet().getVilleDepart() + " → " + s.getTrajet().getVilleArrivee()
                        + " (" + s.getTrajet().getDateDepart() + ")",
                s.getMotif(), s.getDetails(), s.getDateCreation(), s.isTraite());
    }
}
