package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.ConnexionRequest;
import tn.iset.covoit.dto.InscriptionRequest;
import tn.iset.covoit.dto.UtilisateurResponse;
import tn.iset.covoit.service.AuthService;

/** User story #1 : S'inscrire / Se connecter */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/inscription")
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurResponse inscription(@Valid @RequestBody InscriptionRequest requete) {
        return authService.inscrire(requete);
    }

    @PostMapping("/connexion")
    public UtilisateurResponse connexion(@Valid @RequestBody ConnexionRequest requete) {
        return authService.connecter(requete);
    }
}
