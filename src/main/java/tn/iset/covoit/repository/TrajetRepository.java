package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iset.covoit.entity.Trajet;

import java.time.LocalDate;
import java.util.List;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    /** Recherche des trajets à venir avec des places libres (#3). */
    @Query("""
            SELECT t FROM Trajet t
            WHERE LOWER(t.villeDepart) LIKE LOWER(CONCAT('%', :depart, '%'))
              AND LOWER(t.villeArrivee) LIKE LOWER(CONCAT('%', :arrivee, '%'))
              AND (:date IS NULL OR t.dateDepart = :date)
              AND t.dateDepart >= :aujourdhui
              AND t.placesDisponibles > 0
            ORDER BY t.dateDepart, t.heureDepart
            """)
    List<Trajet> rechercher(@Param("depart") String depart,
                            @Param("arrivee") String arrivee,
                            @Param("date") LocalDate date,
                            @Param("aujourdhui") LocalDate aujourdhui);

    /** Trajets à venir d'un conducteur (#2). */
    List<Trajet> findByConducteurIdAndDateDepartGreaterThanEqualOrderByDateDepartAscHeureDepartAsc(
            Long conducteurId, LocalDate date);

    long countByConducteurId(Long conducteurId);
}
