package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iset.covoit.entity.Reservation;
import tn.iset.covoit.entity.StatutReservation;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPassagerIdOrderByDateReservationDesc(Long passagerId);

    /** Toutes les réservations dans un statut donné (ex. : demandes en attente à faire expirer). */
    List<Reservation> findByStatut(StatutReservation statut);

    long countByStatutIn(Collection<StatutReservation> statuts);

    List<Reservation> findByPassagerIdAndStatutIn(Long passagerId, Collection<StatutReservation> statuts);

    /** Le passager a-t-il déjà une demande en attente ou une réservation confirmée sur ce trajet ? */
    boolean existsByTrajetIdAndPassagerIdAndStatutIn(Long trajetId, Long passagerId, Collection<StatutReservation> statuts);

    long countByPassagerIdAndStatut(Long passagerId, StatutReservation statut);

    /** Passagers d'un trajet (vu par le conducteur). */
    List<Reservation> findByTrajetIdAndStatutInOrderByDateReservation(Long trajetId, Collection<StatutReservation> statuts);

    /** Demandes reçues par un conducteur, sur tous ses trajets. */
    List<Reservation> findByTrajetConducteurIdAndStatutOrderByDateReservation(Long conducteurId, StatutReservation statut);

    /** Réservations confirmées qui partent bientôt et n'ont pas encore reçu de rappel (#9). */
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.statut = tn.iset.covoit.entity.StatutReservation.CONFIRMEE
              AND (r.rappelEnvoye IS NULL OR r.rappelEnvoye = false)
              AND r.trajet.dateDepart BETWEEN :debut AND :fin
            """)
    List<Reservation> rappelsAEnvoyer(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}
