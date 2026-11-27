package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.*;
import tn.iset.covoit.security.JwtService;
import tn.iset.covoit.service.AuthService;
import tn.iset.covoit.service.CompteService;

import java.util.Map;

/**
 * User story #1 : S'inscrire / Se connecter. Renvoie un jeton JWT à joindre aux requêtes suivantes.
 * Ces adresses sont publiques (pas de jeton nécessaire).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Map<String, String> OK_EMAIL = Map.of("message",
            "Si un compte correspond à cette adresse, un email vient d'être envoyé.");

    private final AuthService authService;
    private final CompteService compteService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, CompteService compteService, JwtService jwtService) {
        this.authService = authService;
        this.compteService = compteService;
        this.jwtService = jwtService;
    }

    @PostMapping("/inscription")
    @ResponseStatus(HttpStatus.CREATED)
    public ConnexionResponse inscription(@Valid @RequestBody InscriptionRequest requete) {
        UtilisateurResponse u = authService.inscrire(requete);
        // Avec la vérification d'email activée, pas de jeton tant que l'email n'est pas confirmé
        return authService.verificationActive() ? ConnexionResponse.aVerifier(u) : session(u);
    }

    @PostMapping("/connexion")
    public ConnexionResponse connexion(@Valid @RequestBody ConnexionRequest requete) {
        return session(authService.connecter(requete));
    }

    /** Lien reçu par email : confirme l'adresse et connecte directement l'utilisateur. */
    @PostMapping("/verification")
    public ConnexionResponse verification(@RequestParam String jeton) {
        return session(compteService.verifierEmail(jeton));
    }

    @PostMapping("/verification/renvoi")
    public Map<String, String> renvoyerVerification(@Valid @RequestBody EmailRequest requete) {
        compteService.renvoyerVerification(requete.email());
        return OK_EMAIL;
    }

    @PostMapping("/mot-de-passe-oublie")
    public Map<String, String> motDePasseOublie(@Valid @RequestBody EmailRequest requete) {
        compteService.motDePasseOublie(requete.email());
        return OK_EMAIL;
    }

    @PostMapping("/reinitialisation")
    public Map<String, String> reinitialiser(@Valid @RequestBody ReinitialisationRequest requete) {
        compteService.reinitialiser(requete.jeton(), requete.motDePasse());
        return Map.of("message", "Mot de passe modifié. Vous pouvez vous connecter.");
    }

    private ConnexionResponse session(UtilisateurResponse utilisateur) {
        return ConnexionResponse.connecte(jwtService.creerJeton(utilisateur.id()), utilisateur);
    }
}
