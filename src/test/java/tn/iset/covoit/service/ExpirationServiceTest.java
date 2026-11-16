package tn.iset.covoit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.iset.covoit.Donnees;
import tn.iset.covoit.entity.*;
import tn.iset.covoit.repository.ReservationRepository;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExpirationServiceTest {

    /** « Maintenant » fixé : samedi 14 novembre 2026 à 10:00. */
    static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 14, 10, 0);

    @Mock ReservationRepository reservationRepository;
    @Mock NotificationService notificationService;

    ExpirationService service;
    Utilisateur conducteur;
    Utilisateur passager;

    @BeforeEach
    void preparer() {
        Clock horloge = Clock.fixed(MAINTENANT.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new ExpirationService(reservationRepository, notificationService, horloge);
        conducteur = Donnees.utilisateur(1, "Islem", Role.CONDUCTEUR);
        passager = Donnees.utilisateur(2, "Sara", Role.PASSAGER);
    }

    /** Demande faite il y a « ilYaHeures » heures, pour un départ dans « departDansHeures » heures. */
    private Reservation demande(long ilYaHeures, long departDansHeures, int placesLibres) {
        LocalDateTime depart = MAINTENANT.plusHours(departDansHeures);
        Trajet t = Donnees.trajetLe(10, conducteur, depart.toLocalDate(), depart.toLocalTime(), placesLibres);
        Reservation r = Donnees.reservation(5, t, passager, 2, StatutReservation.EN_ATTENTE);
        r.setDateReservation(MAINTENANT.minusHours(ilYaHeures));
        return r;
    }

    @Test
    void uneDemandeRecenteEtUnDepartLointainNExpirePas() {
        assertFalse(ExpirationService.doitExpirer(demande(1, 48, 1), MAINTENANT));
    }

    @Test
    void uneDemandeSansReponseDepuis12hExpire() {
        assertTrue(ExpirationService.doitExpirer(demande(12, 48, 1), MAINTENANT));
        assertFalse(ExpirationService.doitExpirer(demande(11, 48, 1), MAINTENANT));
    }

    @Test
    void uneDemandeExpireDeuxHeuresAvantLeDepart() {
        assertTrue(ExpirationService.doitExpirer(demande(1, 2, 1), MAINTENANT));
        assertFalse(ExpirationService.doitExpirer(demande(1, 3, 1), MAINTENANT));
    }

    @Test
    void uneReservationDejaConfirmeeNExpireJamais() {
        Reservation r = demande(30, 1, 1);
        r.setStatut(StatutReservation.CONFIRMEE);

        assertFalse(ExpirationService.doitExpirer(r, MAINTENANT));
    }

    @Test
    void lExpirationRendLesPlacesEtPrevientLePassager() {
        Reservation ancienne = demande(13, 48, 1);
        Reservation recente = demande(1, 48, 1);
        when(reservationRepository.findByStatut(StatutReservation.EN_ATTENTE)).thenReturn(List.of(ancienne, recente));

        int nombre = service.expirerDemandes();

        assertEquals(1, nombre);
        assertEquals(StatutReservation.EXPIREE, ancienne.getStatut());
        assertEquals(3, ancienne.getTrajet().getPlacesDisponibles(), "1 + 2 places rendues");
        assertEquals(StatutReservation.EN_ATTENTE, recente.getStatut());
        verify(notificationService, times(1)).notifier(eq(passager), eq(TypeNotification.RESERVATION_EXPIREE),
                anyString(), anyString(), eq("recherche.html"));
    }
}
