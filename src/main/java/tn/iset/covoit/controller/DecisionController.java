package tn.iset.covoit.controller;

import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.DecisionService;

import java.util.List;

/** Le conducteur accepte ou refuse les demandes de réservation. */
@RestController
@RequestMapping("/api/reservations")
public class DecisionController {

    private final DecisionService decisionService;
    private final UtilisateurCourant courant;

    public DecisionController(DecisionService decisionService, UtilisateurCourant courant) {
        this.courant = courant;
        this.decisionService = decisionService;
    }

    @GetMapping("/conducteur/{conducteurId}/demandes")
    public List<ReservationResponse> demandes(@PathVariable Long conducteurId) {
        courant.verifier(conducteurId);
        return decisionService.demandesEnAttente(conducteurId);
    }

    @PutMapping("/{id}/acceptation")
    public ReservationResponse accepter(@PathVariable Long id, @RequestParam Long conducteurId) {
        courant.verifier(conducteurId);
        return decisionService.accepter(id, conducteurId);
    }

    @PutMapping("/{id}/refus")
    public ReservationResponse refuser(@PathVariable Long id, @RequestParam Long conducteurId) {
        courant.verifier(conducteurId);
        return decisionService.refuser(id, conducteurId);
    }
}
