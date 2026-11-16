package tn.iset.covoit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import tn.iset.covoit.Donnees;
import tn.iset.covoit.entity.*;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecisionServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock NotificationService notificationService;

    DecisionService service;
    Utilisateur conducteur;
    Utilisateur passager;
    Trajet trajet;
    Reservation demande;

    @BeforeEach
    void preparer() {
        service = new DecisionService(reservationRepository, notificationService);
        conducteur = Donnees.utilisateur(1, "Islem", Role.CONDUCTEUR);
        passager = Donnees.utilisateur(2, "Sara", Role.PASSAGER);
        trajet = Donnees.trajet(10, conducteur, 24, 1);               // 2 places déjà bloquées par la demande
        demande = Donnees.reservation(5, trajet, passager, 2, StatutReservation.EN_ATTENTE);
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(demande));
    }

    @Test
    void leConducteurAccepteUneDemande() {
        service.accepter(5L, 1L);

        assertEquals(StatutReservation.CONFIRMEE, demande.getStatut());
        assertNotNull(demande.getDateDecision());
        assertEquals(1, trajet.getPlacesDisponibles(), "les places restent prises");
        verify(notificationService).notifier(eq(passager), eq(TypeNotification.RESERVATION_ACCEPTEE),
                anyString(), anyString(), anyString());
    }

    @Test
    void refuserRendLesPlacesAuTrajet() {
        service.refuser(5L, 1L);

        assertEquals(StatutReservation.REFUSEE, demande.getStatut());
        assertEquals(3, trajet.getPlacesDisponibles(), "1 + 2 places rendues");
        verify(notificationService).notifier(eq(passager), eq(TypeNotification.RESERVATION_REFUSEE),
                anyString(), anyString(), anyString());
    }

    @Test
    void unAutreConducteurNePeutPasRepondre() {
        ApiException e = assertThrows(ApiException.class, () -> service.accepter(5L, 99L));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals(StatutReservation.EN_ATTENTE, demande.getStatut());
        verifyNoInteractions(notificationService);
    }

    @Test
    void onNeRepondPasDeuxFoisALaMemeDemande() {
        demande.setStatut(StatutReservation.CONFIRMEE);

        ApiException e = assertThrows(ApiException.class, () -> service.refuser(5L, 1L));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals(1, trajet.getPlacesDisponibles(), "aucune place rendue par erreur");
    }

    @Test
    void uneDemandeInconnueDonne404() {
        when(reservationRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class, () -> service.accepter(404L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
}
