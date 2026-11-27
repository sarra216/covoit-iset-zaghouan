package tn.iset.covoit.controller;

import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.AdminStatistiques;
import tn.iset.covoit.dto.AdminUtilisateur;
import tn.iset.covoit.dto.SignalementResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.AdminService;

import java.util.List;

/** Administration. Toutes ces adresses exigent le rôle ADMIN (voir SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UtilisateurCourant courant;

    public AdminController(AdminService adminService, UtilisateurCourant courant) {
        this.adminService = adminService;
        this.courant = courant;
    }

    @GetMapping("/statistiques")
    public AdminStatistiques statistiques() {
        return adminService.statistiques();
    }

    @GetMapping("/utilisateurs")
    public List<AdminUtilisateur> utilisateurs(@RequestParam(required = false) String q) {
        return adminService.utilisateurs(q);
    }

    @PutMapping("/utilisateurs/{id}/suspension")
    public AdminUtilisateur suspendre(@PathVariable Long id, @RequestParam boolean suspendu) {
        return adminService.suspendre(id, suspendu, courant.id());
    }

    @GetMapping("/signalements")
    public List<SignalementResponse> signalements(@RequestParam(defaultValue = "false") boolean traites) {
        return adminService.signalements(traites);
    }

    @PutMapping("/signalements/{id}/traitement")
    public SignalementResponse traiter(@PathVariable Long id) {
        return adminService.traiter(id, courant.id());
    }
}
