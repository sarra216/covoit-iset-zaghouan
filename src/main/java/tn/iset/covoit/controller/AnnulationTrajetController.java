package tn.iset.covoit.controller;

import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.TrajetResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.AnnulationTrajetService;

/** Le conducteur annule un de ses trajets. */
@RestController
@RequestMapping("/api/trajets")
public class AnnulationTrajetController {

    private final AnnulationTrajetService annulationTrajetService;
    private final UtilisateurCourant courant;

    public AnnulationTrajetController(AnnulationTrajetService annulationTrajetService, UtilisateurCourant courant) {
        this.annulationTrajetService = annulationTrajetService;
        this.courant = courant;
    }

    @PutMapping("/{id}/annulation")
    public TrajetResponse annuler(@PathVariable Long id, @RequestParam Long conducteurId) {
        courant.verifier(conducteurId);
        return annulationTrajetService.annuler(id, conducteurId);
    }
}
