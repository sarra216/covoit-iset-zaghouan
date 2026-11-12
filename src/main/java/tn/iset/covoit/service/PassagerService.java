package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ProfilPassagerResponse;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

/** User story #11 : Consulter le profil du passager */
@Service
public class PassagerService {

    private final UtilisateurRepository utilisateurRepository;
    private final ReservationRepository reservationRepository;

    public PassagerService(UtilisateurRepository utilisateurRepository, ReservationRepository reservationRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public ProfilPassagerResponse profil(Long id) {
        Utilisateur passager = utilisateurRepository.findById(id)
                .orElseThrow(() -> ApiException.introuvable("Passager introuvable"));

        return new ProfilPassagerResponse(
                passager.getId(),
                passager.getNom(),
                passager.getPrenom(),
                passager.getDateInscription().toLocalDate(),
                reservationRepository.countByPassagerIdAndStatut(id, StatutReservation.CONFIRMEE),
                reservationRepository.countByPassagerIdAndStatut(id, StatutReservation.ANNULEE));
    }
}
