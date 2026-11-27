package tn.iset.covoit.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.entity.TypeNotification;
import tn.iset.covoit.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** User story #9 : Notification de départ, envoyée moins de 24 h avant le trajet. */
@Service
public class RappelService {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public RappelService(ReservationRepository reservationRepository, NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    /** Vérifie toutes les 15 minutes (et une minute après le démarrage). */
    @Scheduled(initialDelay = 60_000, fixedDelay = 900_000)
    @Transactional
    public void envoyerRappels() {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime limite = maintenant.plusHours(24);
        for (Reservation r : reservationRepository.rappelsAEnvoyer(LocalDate.now(), LocalDate.now().plusDays(1))) {
            Trajet t = r.getTrajet();
            LocalDateTime depart = LocalDateTime.of(t.getDateDepart(), t.getHeureDepart());
            if (depart.isAfter(maintenant) && depart.isBefore(limite)) {
                notificationService.notifier(r.getPassager(), TypeNotification.RAPPEL_DEPART,
                        "Départ bientôt",
                        "Votre trajet " + t.getVilleDepart() + " → " + t.getVilleArrivee() + " part le "
                                + t.getDateDepart() + " à " + t.getHeureDepart()
                                + (t.getLieuDepart() != null ? " depuis " + t.getLieuDepart() : "") + ".",
                        "trajet.html?id=" + t.getId());
                r.setRappelEnvoye(true);
            }
        }
    }
}
