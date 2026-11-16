package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iset.covoit.entity.Role;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iset.covoit.entity.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRole(Role role);

    /** Recherche pour l'administration (nom, prénom ou email). */
    @Query("""
            SELECT u FROM Utilisateur u
            WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY u.dateInscription DESC
            """)
    List<Utilisateur> rechercher(@Param("q") String q);
}
