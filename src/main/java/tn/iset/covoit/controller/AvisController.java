package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.AvisRequest;
import tn.iset.covoit.dto.AvisResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.AvisService;

import java.util.List;

/** User story #8 : Noter un conducteur. */
@RestController
@RequestMapping("/api/avis")
public class AvisController {

    private final AvisService avisService;
    private final UtilisateurCourant courant;

    public AvisController(AvisService avisService, UtilisateurCourant courant) {
        this.courant = courant;
        this.avisService = avisService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvisResponse noter(@Valid @RequestBody AvisRequest requete) {
        courant.verifier(requete.passagerId());
        return avisService.noter(requete);
    }

    @GetMapping("/conducteur/{conducteurId}")
    public List<AvisResponse> avisDuConducteur(@PathVariable Long conducteurId) {
        return avisService.avisDuConducteur(conducteurId);
    }
}
