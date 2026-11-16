package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.AvisRequest;
import tn.iset.covoit.dto.AvisResponse;
import tn.iset.covoit.entity.Avis;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.TypeNotification;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.AvisRepository;
import tn.iset.covoit.repository.ReservationRepository;

import java.util.List;

/** User story #8 : Noter un conducteur, après un trajet effectué. */
@Service
public class AvisService {

    private final AvisRepository avisRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public AvisService(AvisRepository avisRepository, ReservationRepository reservationRepository,
                       NotificationService notificationService) {
        this.avisRepository = avisRepository;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public AvisResponse noter(AvisRequest requete) {
        Reservation reservation = reservationRepository.findById(requete.reservationId())
                .orElseThrow(() -> ApiException.introuvable("Réservation introuvable"));
        if (!reservation.getPassager().getId().equals(requete.passagerId())) {
            throw ApiException.interdit("Seul le passager de cette réservation peut noter le conducteur");
        }
        if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
            throw ApiException.requeteInvalide("Seul un trajet confirmé peut être noté");
        }
        if (!reservation.getTrajet().estPasse()) {
            throw ApiException.requeteInvalide("Vous pourrez noter le conducteur après le trajet");
        }
        if (avisRepository.existsByReservationId(reservation.getId())) {
            throw ApiException.conflit("Vous avez déjà noté ce trajet");
        }

        Utilisateur conducteur = reservation.getTrajet().getConducteur();
        Avis avis = new Avis();
        avis.setReservation(reservation);
        avis.setConducteur(conducteur);
        avis.setPassager(reservation.getPassager());
        avis.setNote(requete.note());
        if (requete.commentaire() != null && !requete.commentaire().isBlank()) {
            avis.setCommentaire(requete.commentaire().trim());
        }
        avis = avisRepository.save(avis);

        // Moyenne mise à jour sans tout recalculer : (ancienne moyenne × n + nouvelle note) / (n + 1)
        int n = conducteur.getNombreAvis();
        double ancienne = conducteur.getNoteMoyenne() == null ? 0 : conducteur.getNoteMoyenne();
        double moyenne = (ancienne * n + requete.note()) / (n + 1);
        conducteur.setNoteMoyenne(Math.round(moyenne * 10) / 10.0);
        conducteur.setNombreAvis(n + 1);

        notificationService.notifier(conducteur, TypeNotification.NOUVEL_AVIS,
                "Nouvel avis : " + requete.note() + "/5",
                reservation.getPassager().getNomComplet() + " vous a noté après le trajet "
                        + reservation.getTrajet().getVilleDepart() + " → " + reservation.getTrajet().getVilleArrivee() + ".",
                "profil-conducteur.html?id=" + conducteur.getId());

        return AvisResponse.from(avis);
    }

    @Transactional(readOnly = true)
    public List<AvisResponse> avisDuConducteur(Long conducteurId) {
        return avisRepository.findByConducteurIdOrderByDateCreationDesc(conducteurId)
                .stream()
                .map(AvisResponse::from)
                .toList();
    }
}
