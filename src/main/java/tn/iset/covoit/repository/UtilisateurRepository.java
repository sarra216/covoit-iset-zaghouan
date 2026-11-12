package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iset.covoit.entity.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);
}
