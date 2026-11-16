package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iset.covoit.dto.RoutePopulaire;
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
              AND (t.annule IS NULL OR t.annule = false)
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

    long countByDateDepartGreaterThanEqual(LocalDate date);

    /** Tous les trajets d'un conducteur, du plus récent au plus ancien (« Mes trajets »). */
    List<Trajet> findByConducteurIdOrderByDateDepartDescHeureDepartDesc(Long conducteurId);

    /** Itinéraires les plus proposés parmi les trajets à venir qui ont des places libres. */
    @Query("""
            SELECT new tn.iset.covoit.dto.RoutePopulaire(t.villeDepart, t.villeArrivee, COUNT(t), MIN(t.prix))
            FROM Trajet t
            WHERE t.dateDepart >= :aujourdhui AND t.placesDisponibles > 0
              AND (t.annule IS NULL OR t.annule = false)
            GROUP BY t.villeDepart, t.villeArrivee
            ORDER BY COUNT(t) DESC
            """)
    List<RoutePopulaire> routesPopulaires(@Param("aujourdhui") LocalDate aujourdhui);

    /** Villes déjà utilisées dans des trajets (reconnues par l'assistant). */
    @Query("SELECT DISTINCT t.villeDepart FROM Trajet t")
    List<String> villesDepart();

    @Query("SELECT DISTINCT t.villeArrivee FROM Trajet t")
    List<String> villesArrivee();
}
