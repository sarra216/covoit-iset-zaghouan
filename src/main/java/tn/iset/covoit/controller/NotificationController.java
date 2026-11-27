package tn.iset.covoit.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.iset.covoit.dto.NotificationResponse;
import tn.iset.covoit.security.UtilisateurCourant;
import tn.iset.covoit.service.NotificationService;

import java.util.List;
import java.util.Map;

/** User story #9 : notifications (cloche). */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurCourant courant;

    public NotificationController(NotificationService notificationService, UtilisateurCourant courant) {
        this.courant = courant;
        this.notificationService = notificationService;
    }

    @GetMapping("/{utilisateurId}")
    public List<NotificationResponse> lister(@PathVariable Long utilisateurId) {
        courant.verifier(utilisateurId);
        return notificationService.lister(utilisateurId);
    }

    @GetMapping("/{utilisateurId}/non-lues")
    public Map<String, Long> nonLues(@PathVariable Long utilisateurId) {
        courant.verifier(utilisateurId);
        return Map.of("nombre", notificationService.nombreNonLues(utilisateurId));
    }

    @PutMapping("/{utilisateurId}/lues")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void toutMarquerLu(@PathVariable Long utilisateurId) {
        courant.verifier(utilisateurId);
        notificationService.toutMarquerLu(utilisateurId);
    }
}
