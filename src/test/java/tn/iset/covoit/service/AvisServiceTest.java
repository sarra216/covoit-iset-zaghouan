package tn.iset.covoit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.iset.covoit.Donnees;
import tn.iset.covoit.dto.AvisRequest;
import tn.iset.covoit.entity.*;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.AvisRepository;
import tn.iset.covoit.repository.ReservationRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AvisServiceTest {

    @Mock AvisRepository avisRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock NotificationService notificationService;

    AvisService service;
    Utilisateur conducteur;
    Utilisateur passager;
    Reservation reservation;

    @BeforeEach
    void preparer() {
        service = new AvisService(avisRepository, reservationRepository, notificationService);
        conducteur = Donnees.utilisateur(1, "Islem", Role.CONDUCTEUR);
        passager = Donnees.utilisateur(2, "Sara", Role.PASSAGER);
        Trajet trajetPasse = Donnees.trajet(10, conducteur, -24, 0);
        reservation = Donnees.reservation(5, trajetPasse, passager, 1, StatutReservation.CONFIRMEE);

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        when(avisRepository.save(any(Avis.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    @Test
    void laMoyenneDuConducteurEstMiseAJour() {
        conducteur.setNoteMoyenne(4.0);
        conducteur.setNombreAvis(3);

        service.noter(new AvisRequest(5L, 2L, 5, "Très ponctuel"));

        assertEquals(4.3, conducteur.getNoteMoyenne().doubleValue(), 0.001, "(4 × 3 + 5) / 4 = 4,25 arrondi à 4,3");
        assertEquals(4, conducteur.getNombreAvis());
        verify(notificationService).notifier(eq(conducteur), eq(TypeNotification.NOUVEL_AVIS),
                anyString(), anyString(), anyString());
    }

    @Test
    void lePremierAvisDevientLaMoyenne() {
        service.noter(new AvisRequest(5L, 2L, 4, null));

        assertEquals(4.0, conducteur.getNoteMoyenne().doubleValue(), 0.001);
        assertEquals(1, conducteur.getNombreAvis());
    }

    @Test
    void onNeNotePasAvantLeTrajet() {
        reservation.setTrajet(Donnees.trajet(11, conducteur, 24, 0));

        assertThrows(ApiException.class, () -> service.noter(new AvisRequest(5L, 2L, 5, null)));
        verify(avisRepository, never()).save(any());
    }

    @Test
    void onNeNotePasDeuxFois() {
        when(avisRepository.existsByReservationId(5L)).thenReturn(true);

        assertThrows(ApiException.class, () -> service.noter(new AvisRequest(5L, 2L, 5, null)));
    }

    @Test
    void seulLePassagerDeLaReservationPeutNoter() {
        assertThrows(ApiException.class, () -> service.noter(new AvisRequest(5L, 99L, 5, null)));
    }

    @Test
    void uneDemandeRefuseeNePeutPasEtreNotee() {
        reservation.setStatut(StatutReservation.REFUSEE);

        assertThrows(ApiException.class, () -> service.noter(new AvisRequest(5L, 2L, 5, null)));
    }
}
