package tn.iset.covoit.controller;

import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.ProfilPassagerResponse;
import tn.iset.covoit.service.PassagerService;

/** User story #11 : Consulter le profil du passager */
@RestController
@RequestMapping("/api/passagers")
public class PassagerController {

    private final PassagerService passagerService;

    public PassagerController(PassagerService passagerService) {
        this.passagerService = passagerService;
    }

    @GetMapping("/{id}")
    public ProfilPassagerResponse profil(@PathVariable Long id) {
        return passagerService.profil(id);
    }
}
