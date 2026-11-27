package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.MessageRequest;
import tn.iset.covoit.dto.MessageResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.MessageService;

import java.util.List;

/** User story #7 : Contacter le conducteur */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UtilisateurCourant courant;

    public MessageController(MessageService messageService, UtilisateurCourant courant) {
        this.courant = courant;
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse envoyer(@Valid @RequestBody MessageRequest requete) {
        courant.verifier(requete.expediteurId());
        return messageService.envoyer(requete);
    }

    @GetMapping("/conversation")
    public List<MessageResponse> conversation(@RequestParam Long utilisateur1, @RequestParam Long utilisateur2) {
        courant.verifierUnDe(utilisateur1, utilisateur2);
        return messageService.conversation(utilisateur1, utilisateur2);
    }

    @GetMapping("/utilisateur/{id}")
    public List<MessageResponse> messagesDe(@PathVariable Long id) {
        courant.verifier(id);
        return messageService.messagesDe(id);
    }
}
