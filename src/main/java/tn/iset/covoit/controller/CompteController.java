package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.BlocageRequest;
import tn.iset.covoit.dto.BlocageResponse;
import tn.iset.covoit.dto.SignalementRequest;
import tn.iset.covoit.dto.SuppressionCompteRequest;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.BlocageService;
import tn.iset.covoit.service.CompteService;
import tn.iset.covoit.service.SignalementService;

import java.util.List;

/** Sécurité entre utilisateurs (signaler, bloquer) et suppression du compte. */
@RestController
@RequestMapping("/api")
public class CompteController {

    private final CompteService compteService;
    private final SignalementService signalementService;
    private final BlocageService blocageService;
    private final UtilisateurCourant courant;

    public CompteController(CompteService compteService, SignalementService signalementService,
                            BlocageService blocageService, UtilisateurCourant courant) {
        this.compteService = compteService;
        this.signalementService = signalementService;
        this.blocageService = blocageService;
        this.courant = courant;
    }

    @PostMapping("/utilisateurs/{id}/suppression")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerCompte(@PathVariable Long id, @Valid @RequestBody SuppressionCompteRequest requete) {
        courant.verifier(id);
        compteService.supprimer(id, requete.motDePasse());
    }

    @PostMapping("/signalements")
    @ResponseStatus(HttpStatus.CREATED)
    public void signaler(@Valid @RequestBody SignalementRequest requete) {
        courant.verifier(requete.auteurId());
        signalementService.signaler(requete);
    }

    @GetMapping("/blocages/{bloqueurId}")
    public List<BlocageResponse> blocages(@PathVariable Long bloqueurId) {
        courant.verifier(bloqueurId);
        return blocageService.liste(bloqueurId);
    }

    @PostMapping("/blocages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bloquer(@Valid @RequestBody BlocageRequest requete) {
        courant.verifier(requete.bloqueurId());
        blocageService.bloquer(requete.bloqueurId(), requete.bloqueId());
    }

    @DeleteMapping("/blocages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void debloquer(@RequestParam Long bloqueurId, @RequestParam Long bloqueId) {
        courant.verifier(bloqueurId);
        blocageService.debloquer(bloqueurId, bloqueId);
    }
}
