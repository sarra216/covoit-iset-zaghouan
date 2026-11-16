package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.ReservationRequest;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.ReservationService;

import java.util.List;

/** User story #4 : Réserver une place */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UtilisateurCourant courant;

    public ReservationController(ReservationService reservationService, UtilisateurCourant courant) {
        this.courant = courant;
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserver(@Valid @RequestBody ReservationRequest requete) {
        courant.verifier(requete.passagerId());
        return reservationService.reserver(requete);
    }

    @GetMapping("/passager/{passagerId}")
    public List<ReservationResponse> reservationsDuPassager(@PathVariable Long passagerId) {
        courant.verifier(passagerId);
        return reservationService.reservationsDuPassager(passagerId);
    }
}
