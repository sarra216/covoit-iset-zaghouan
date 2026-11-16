package tn.iset.covoit.controller;

import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.AnnulationService;

/** User story #6 : Annuler une réservation */
@RestController
@RequestMapping("/api/reservations")
public class AnnulationController {

    private final AnnulationService annulationService;
    private final UtilisateurCourant courant;

    public AnnulationController(AnnulationService annulationService, UtilisateurCourant courant) {
        this.courant = courant;
        this.annulationService = annulationService;
    }

    @PutMapping("/{id}/annulation")
    public ReservationResponse annuler(@PathVariable Long id, @RequestParam Long passagerId) {
        courant.verifier(passagerId);
        return annulationService.annuler(id, passagerId);
    }
}
