package tn.iset.covoit.controller;

import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.ProfilConducteurResponse;
import tn.iset.covoit.service.ConducteurService;

/** User story #2 : Consulter le profil du conducteur */
@RestController
@RequestMapping("/api/conducteurs")
public class ConducteurController {

    private final ConducteurService conducteurService;

    public ConducteurController(ConducteurService conducteurService) {
        this.conducteurService = conducteurService;
    }

    @GetMapping("/{id}")
    public ProfilConducteurResponse profil(@PathVariable Long id) {
        return conducteurService.profil(id);
    }
}
