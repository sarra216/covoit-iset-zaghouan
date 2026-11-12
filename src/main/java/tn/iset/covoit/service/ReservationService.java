package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ReservationRequest;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.util.List;

/** User story #4 : Réserver une place */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ReservationService(ReservationRepository reservationRepository, TrajetRepository trajetRepository,
                              UtilisateurRepository utilisateurRepository) {
        this.reservationRepository = reservationRepository;
        this.trajetRepository = trajetRepository;
        this.utilisateurRepository = utilisateurRepository;
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
        if (trajet.estPasse()) {
            throw ApiException.requeteInvalide("Ce trajet est déjà parti");
        }
        if (reservationRepository.existsByTrajetIdAndPassagerIdAndStatut(
                trajet.getId(), passager.getId(), StatutReservation.CONFIRMEE)) {
            throw ApiException.conflit("Vous avez déjà réservé ce trajet");
        }
        if (requete.nombrePlaces() > trajet.getPlacesDisponibles()) {
            throw ApiException.conflit("Il ne reste que " + trajet.getPlacesDisponibles() + " place(s)");
        }

        // Le champ @Version de Trajet bloque deux réservations simultanées de la même place
        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - requete.nombrePlaces());

        Reservation reservation = new Reservation();
        reservation.setTrajet(trajet);
        reservation.setPassager(passager);
        reservation.setNombrePlaces(requete.nombrePlaces());

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> reservationsDuPassager(Long passagerId) {
        return reservationRepository.findByPassagerIdOrderByDateReservationDesc(passagerId)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
