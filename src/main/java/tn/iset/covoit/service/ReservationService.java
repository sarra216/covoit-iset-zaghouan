package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ReservationRequest;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.entity.TypeNotification;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.AvisRepository;
import tn.iset.covoit.repository.BlocageRepository;
import tn.iset.covoit.repository.ReservationRepository;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User story #4 : Réserver une place.
 * La réservation est d'abord « en attente » : le conducteur doit l'accepter (DecisionService).
 * Les places sont bloquées dès la demande, pour éviter la surréservation.
 */
@Service
public class ReservationService {

    private static final List<StatutReservation> ACTIVES = List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE);

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AvisRepository avisRepository;
    private final NotificationService notificationService;
    private final BlocageRepository blocageRepository;

    public ReservationService(ReservationRepository reservationRepository, TrajetRepository trajetRepository,
                              UtilisateurRepository utilisateurRepository, AvisRepository avisRepository,
                              NotificationService notificationService, BlocageRepository blocageRepository) {
        this.blocageRepository = blocageRepository;
        this.reservationRepository = reservationRepository;
        this.trajetRepository = trajetRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.avisRepository = avisRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReservationResponse reserver(ReservationRequest requete) {
        Trajet trajet = trajetRepository.findById(requete.trajetId())
                .orElseThrow(() -> ApiException.introuvable("Trajet introuvable"));
        Utilisateur passager = utilisateurRepository.findById(requete.passagerId())
                .orElseThrow(() -> ApiException.introuvable("Passager introuvable"));

        if (trajet.getConducteur().getId().equals(passager.getId())) {
            throw ApiException.requeteInvalide("Vous ne pouvez pas réserver votre propre trajet");
        }
        if (trajet.isAnnule()) {
            throw ApiException.requeteInvalide("Ce trajet a été annulé par le conducteur");
        }
        if (trajet.estPasse()) {
            throw ApiException.requeteInvalide("Ce trajet est déjà parti");
        }
        if (blocageRepository.entre(trajet.getConducteur().getId(), passager.getId())) {
            throw ApiException.interdit("Ce trajet n'est pas disponible pour vous");
        }
        if (reservationRepository.existsByTrajetIdAndPassagerIdAndStatutIn(trajet.getId(), passager.getId(), ACTIVES)) {
            throw ApiException.conflit("Vous avez déjà une demande ou une réservation sur ce trajet");
        }
        if (requete.nombrePlaces() > trajet.getPlacesDisponibles()) {
            throw ApiException.conflit("Il ne reste que " + trajet.getPlacesDisponibles() + " place(s)");
        }

        // Le champ @Version de Trajet bloque deux demandes simultanées sur la même place
        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - requete.nombrePlaces());

        Reservation reservation = new Reservation();
        reservation.setTrajet(trajet);
        reservation.setPassager(passager);
        reservation.setNombrePlaces(requete.nombrePlaces());
        reservation.setStatut(StatutReservation.EN_ATTENTE);
        reservation = reservationRepository.save(reservation);

        notificationService.notifier(trajet.getConducteur(), TypeNotification.DEMANDE_RESERVATION,
                "Nouvelle demande de réservation",
                passager.getNomComplet() + " demande " + requete.nombrePlaces() + " place(s) pour "
                        + trajet.getVilleDepart() + " → " + trajet.getVilleArrivee() + " le " + trajet.getDateDepart() + ".",
                "reservations.html?onglet=demandes");

        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> reservationsDuPassager(Long passagerId) {
        Set<Long> notees = avisRepository.findByPassagerId(passagerId).stream()
                .map(a -> a.getReservation().getId())
                .collect(Collectors.toSet());
        return reservationRepository.findByPassagerIdOrderByDateReservationDesc(passagerId)
                .stream()
                .map(r -> ReservationResponse.from(r, notees.contains(r.getId())))
                .toList();
    }
}
