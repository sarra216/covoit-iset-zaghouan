package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPassagerIdOrderByDateReservationDesc(Long passagerId);

    boolean existsByTrajetIdAndPassagerIdAndStatut(Long trajetId, Long passagerId, StatutReservation statut);

    long countByPassagerIdAndStatut(Long passagerId, StatutReservation statut);
}
