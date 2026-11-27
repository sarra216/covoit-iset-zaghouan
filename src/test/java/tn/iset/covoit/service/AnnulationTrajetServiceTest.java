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
import tn.iset.covoit.repository.TrajetRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnnulationTrajetServiceTest {

    @Mock TrajetRepository trajetRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock NotificationService notificationService;

    AnnulationTrajetService service;
    Utilisateur conducteur;
    Trajet trajet;
    Reservation confirmee;
    Reservation enAttente;

    @BeforeEach
    void preparer() {
        service = new AnnulationTrajetService(trajetRepository, reservationRepository, notificationService);
        conducteur = Donnees.utilisateur(1, "Islem", Role.CONDUCTEUR);
        trajet = Donnees.trajet(10, conducteur, 24, 1);
        confirmee = Donnees.reservation(5, trajet, Donnees.utilisateur(2, "Sara", Role.PASSAGER), 1, StatutReservation.CONFIRMEE);
        enAttente = Donnees.reservation(6, trajet, Donnees.utilisateur(3, "Ines", Role.PASSAGER), 2, StatutReservation.EN_ATTENTE);

        when(trajetRepository.findById(10L)).thenReturn(Optional.of(trajet));
        when(reservationRepository.findByTrajetIdAndStatutInOrderByDateReservation(eq(10L), anyCollection()))
                .thenReturn(List.of(confirmee, enAttente));
    }

    @Test
    void annulerLeTrajetFermeToutesLesReservationsEtPrevientLesPassagers() {
        service.annuler(10L, 1L);

        assertTrue(trajet.isAnnule());
        assertEquals(StatutReservation.ANNULEE, confirmee.getStatut());
        assertEquals(StatutReservation.ANNULEE, enAttente.getStatut());
        verify(notificationService, times(2)).notifier(any(), eq(TypeNotification.TRAJET_ANNULE),
                anyString(), anyString(), anyString());
    }

    @Test
    void seulLeConducteurPeutAnnuler() {
        ApiException e = assertThrows(ApiException.class, () -> service.annuler(10L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertFalse(trajet.isAnnule());
        verifyNoInteractions(notificationService);
    }

    @Test
    void onNAnnulePasDeuxFois() {
        trajet.setAnnule(true);

        assertThrows(ApiException.class, () -> service.annuler(10L, 1L));
    }

    @Test
    void onNAnnulePasUnTrajetDejaParti() {
        Trajet parti = Donnees.trajet(11, conducteur, -1, 0);
        when(trajetRepository.findById(11L)).thenReturn(Optional.of(parti));

        assertThrows(ApiException.class, () -> service.annuler(11L, 1L));
    }
}
