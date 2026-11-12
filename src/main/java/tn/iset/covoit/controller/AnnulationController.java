package tn.iset.covoit.controller;

import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.service.AnnulationService;

/** User story #6 : Annuler une réservation */
@RestController
@RequestMapping("/api/reservations")
public class AnnulationController {

    private final AnnulationService annulationService;

    public AnnulationController(AnnulationService annulationService) {
        this.annulationService = annulationService;
    }

    @PutMapping("/{id}/annulation")
    public ReservationResponse annuler(@PathVariable Long id, @RequestParam Long passagerId) {
        return annulationService.annuler(id, passagerId);
    }
}
