package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.TrajetRequest;
import tn.iset.covoit.dto.TrajetResponse;
import tn.iset.covoit.service.TrajetService;

import java.time.LocalDate;
import java.util.List;

/** User stories #26 : Proposer un trajet  et  #3 : Chercher un trajet */
@RestController
@RequestMapping("/api/trajets")
public class TrajetController {

    private final TrajetService trajetService;

    public TrajetController(TrajetService trajetService) {
        this.trajetService = trajetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrajetResponse proposer(@Valid @RequestBody TrajetRequest requete) {
        return trajetService.proposer(requete);
    }

    @GetMapping("/recherche")
    public List<TrajetResponse> rechercher(
            @RequestParam(required = false) String depart,
            @RequestParam(required = false) String arrivee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return trajetService.rechercher(depart, arrivee, date);
    }
}
