package tn.iset.covoit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.entity.TypeNotification;
import tn.iset.covoit.repository.ReservationRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Une demande sans réponse ne doit pas bloquer des places indéfiniment.
 * Elle expire automatiquement :
 *  - 12 heures après la demande, si le conducteur n'a toujours pas répondu ;
 *  - ou 2 heures avant le départ (le passager a besoin de temps pour trouver un autre trajet).
 * Les places sont alors rendues au trajet et le passager est prévenu.
 */
@Service
public class ExpirationService {

    static final Duration DELAI_REPONSE = Duration.ofHours(12);
    static final Duration MARGE_AVANT_DEPART = Duration.ofHours(2);

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    private final Clock horloge;

    @Autowired
    public ExpirationService(ReservationRepository reservationRepository, NotificationService notificationService) {
        this(reservationRepository, notificationService, Clock.systemDefaultZone());
    }

    /** Constructeur utilisé par les tests (horloge contrôlée). */
    ExpirationService(ReservationRepository reservationRepository, NotificationService notificationService, Clock horloge) {
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
        this.horloge = horloge;
    }

    /** Toutes les 5 minutes. */
    @Scheduled(initialDelay = 30_000, fixedDelay = 300_000)
    @Transactional
    public int expirerDemandes() {
        LocalDateTime maintenant = LocalDateTime.now(horloge);
        List<Reservation> aExpirer = reservationRepository.findByStatut(StatutReservation.EN_ATTENTE).stream()
                .filter(r -> doitExpirer(r, maintenant))
                .toList();
        for (Reservation r : aExpirer) {
            expirer(r, maintenant);
        }
        return aExpirer.size();
    }

    /** Règle métier, isolée pour être testée facilement. */
    static boolean doitExpirer(Reservation r, LocalDateTime maintenant) {
        if (r.getStatut() != StatutReservation.EN_ATTENTE) return false;
        Trajet t = r.getTrajet();
        LocalDateTime depart = LocalDateTime.of(t.getDateDepart(), t.getHeureDepart());
        boolean tropTard = !maintenant.isBefore(r.getDateReservation().plus(DELAI_REPONSE));
        boolean departProche = !maintenant.isBefore(depart.minus(MARGE_AVANT_DEPART));
        return tropTard || departProche;
    }

    private void expirer(Reservation r, LocalDateTime maintenant) {
        r.setStatut(StatutReservation.EXPIREE);
        r.setDateDecision(maintenant);
        Trajet t = r.getTrajet();
        t.setPlacesDisponibles(t.getPlacesDisponibles() + r.getNombrePlaces());

        notificationService.notifier(r.getPassager(), TypeNotification.RESERVATION_EXPIREE,
                "Demande expirée",
                "Le conducteur n'a pas répondu à temps pour " + t.getVilleDepart() + " → " + t.getVilleArrivee()
                        + " le " + t.getDateDepart() + ". Cherchez un autre trajet.",
                "recherche.html");
    }
}
