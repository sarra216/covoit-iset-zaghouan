package tn.iset.covoit.controller;

import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.PassagerTrajet;
import tn.iset.covoit.dto.TrajetResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.MesTrajetsService;

import java.util.List;

/** « Mes trajets » côté conducteur. */
@RestController
@RequestMapping("/api/trajets")
public class MesTrajetsController {

    private final MesTrajetsService mesTrajetsService;
    private final UtilisateurCourant courant;

    public MesTrajetsController(MesTrajetsService mesTrajetsService, UtilisateurCourant courant) {
        this.courant = courant;
        this.mesTrajetsService = mesTrajetsService;
    }

    @GetMapping("/conducteur/{conducteurId}")
    public List<TrajetResponse> trajetsDuConducteur(@PathVariable Long conducteurId) {
        courant.verifier(conducteurId);
        return mesTrajetsService.trajetsDuConducteur(conducteurId);
    }

    @GetMapping("/{trajetId}/passagers")
    public List<PassagerTrajet> passagers(@PathVariable Long trajetId, @RequestParam Long conducteurId) {
        courant.verifier(conducteurId);
        return mesTrajetsService.passagersDuTrajet(trajetId, conducteurId);
    }
}
