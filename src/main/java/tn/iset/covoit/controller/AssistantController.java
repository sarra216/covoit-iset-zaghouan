package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.AssistantRequest;
import tn.iset.covoit.dto.AssistantResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.AssistantService;

/** User story #10 : assistant (chatbot). */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final UtilisateurCourant courant;

    public AssistantController(AssistantService assistantService, UtilisateurCourant courant) {
        this.courant = courant;
        this.assistantService = assistantService;
    }

    @PostMapping
    public AssistantResponse repondre(@Valid @RequestBody AssistantRequest requete) {
        courant.verifier(requete.utilisateurId());
        return assistantService.repondre(requete);
    }
}
