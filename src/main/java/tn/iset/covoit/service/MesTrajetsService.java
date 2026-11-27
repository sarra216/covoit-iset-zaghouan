package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.PassagerTrajet;
import tn.iset.covoit.dto.TrajetResponse;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.ReservationRepository;
import tn.iset.covoit.repository.TrajetRepository;

import java.util.List;

/** « Mes trajets » côté conducteur : ses trajets proposés et leurs passagers. */
@Service
public class MesTrajetsService {

    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;

    public MesTrajetsService(TrajetRepository trajetRepository, ReservationRepository reservationRepository) {
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<TrajetResponse> trajetsDuConducteur(Long conducteurId) {
        return trajetRepository.findByConducteurIdOrderByDateDepartDescHeureDepartDesc(conducteurId)
                .stream()
                .map(TrajetResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PassagerTrajet> passagersDuTrajet(Long trajetId, Long conducteurId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> ApiException.introuvable("Trajet introuvable"));
        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            throw ApiException.interdit("Seul le conducteur de ce trajet peut voir ses passagers");
        }
        return reservationRepository
                .findByTrajetIdAndStatutInOrderByDateReservation(trajetId,
                        List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE))
                .stream()
                .map(PassagerTrajet::from)
                .toList();
    }
}
