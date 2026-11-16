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
import tn.iset.covoit.dto.ReservationRequest;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.entity.*;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.AvisRepository;
import tn.iset.covoit.repository.BlocageRepository;
import tn.iset.covoit.repository.ReservationRepository;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock TrajetRepository trajetRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock AvisRepository avisRepository;
    @Mock NotificationService notificationService;
    @Mock BlocageRepository blocageRepository;

    ReservationService service;
    Utilisateur conducteur;
    Utilisateur passager;
    Trajet trajet;

    @BeforeEach
    void preparer() {
        service = new ReservationService(reservationRepository, trajetRepository, utilisateurRepository,
                avisRepository, notificationService, blocageRepository);
        conducteur = Donnees.utilisateur(1, "Islem", Role.CONDUCTEUR);
        passager = Donnees.utilisateur(2, "Sara", Role.PASSAGER);
        trajet = Donnees.trajet(10, conducteur, 24, 3);

        when(trajetRepository.findById(10L)).thenReturn(Optional.of(trajet));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(passager));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    @Test
    void uneDemandeEstEnAttenteEtBloqueLesPlaces() {
        ReservationResponse r = service.reserver(new ReservationRequest(10L, 2L, 2));

        assertEquals(StatutReservation.EN_ATTENTE, r.statut());
        assertEquals(1, trajet.getPlacesDisponibles(), "3 places - 2 demandées");
        verify(notificationService).notifier(eq(conducteur), eq(TypeNotification.DEMANDE_RESERVATION),
                anyString(), anyString(), eq("reservations.html?onglet=demandes"));
    }

    @Test
    void onNePeutPasReserverSonPropreTrajet() {
        ApiException e = assertThrows(ApiException.class, () -> service.reserver(new ReservationRequest(10L, 1L, 1)));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void onNePeutPasDemanderPlusDePlacesQueDisponibles() {
        ApiException e = assertThrows(ApiException.class, () -> service.reserver(new ReservationRequest(10L, 2L, 4)));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals(3, trajet.getPlacesDisponibles(), "aucune place retirée");
    }

    @Test
    void uneSecondeDemandeSurLeMemeTrajetEstRefusee() {
        when(reservationRepository.existsByTrajetIdAndPassagerIdAndStatutIn(eq(10L), eq(2L), anyCollection()))
                .thenReturn(true);

        ApiException e = assertThrows(ApiException.class, () -> service.reserver(new ReservationRequest(10L, 2L, 1)));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void unTrajetAnnuleNePeutPlusEtreReserve() {
        trajet.setAnnule(true);

        assertThrows(ApiException.class, () -> service.reserver(new ReservationRequest(10L, 2L, 1)));
        assertEquals(3, trajet.getPlacesDisponibles());
    }

    @Test
    void unPassagerBloqueParLeConducteurNePeutPasReserver() {
        when(blocageRepository.entre(1L, 2L)).thenReturn(true);

        ApiException e = assertThrows(ApiException.class, () -> service.reserver(new ReservationRequest(10L, 2L, 1)));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void unTrajetDejaPartiNePeutPlusEtreReserve() {
        Trajet parti = Donnees.trajet(11, conducteur, -2, 3);
        when(trajetRepository.findById(11L)).thenReturn(Optional.of(parti));

        assertThrows(ApiException.class, () -> service.reserver(new ReservationRequest(11L, 2L, 1)));
    }
}
