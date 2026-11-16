package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iset.covoit.entity.Avis;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {

    boolean existsByReservationId(Long reservationId);

    List<Avis> findByPassagerId(Long passagerId);

    List<Avis> findByConducteurIdOrderByDateCreationDesc(Long conducteurId);
}
