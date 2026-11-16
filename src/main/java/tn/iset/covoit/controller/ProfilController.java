package tn.iset.covoit.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.ProfilRequest;
import tn.iset.covoit.dto.UtilisateurResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.ProfilService;

/** Compléter / modifier son profil (mode conducteur et véhicule). */
@RestController
@RequestMapping("/api/utilisateurs")
public class ProfilController {

    private final ProfilService profilService;
    private final UtilisateurCourant courant;

    public ProfilController(ProfilService profilService, UtilisateurCourant courant) {
        this.courant = courant;
        this.profilService = profilService;
    }

    @PutMapping("/{id}/profil")
    public UtilisateurResponse mettreAJour(@PathVariable Long id, @Valid @RequestBody ProfilRequest requete) {
        courant.verifier(id);
        return profilService.mettreAJour(id, requete);
    }
}
