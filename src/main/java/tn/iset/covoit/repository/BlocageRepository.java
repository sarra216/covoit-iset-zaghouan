package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iset.covoit.entity.Blocage;

import java.util.List;
import java.util.Optional;

public interface BlocageRepository extends JpaRepository<Blocage, Long> {
    boolean existsByBloqueurIdAndBloqueId(Long bloqueurId, Long bloqueId);
    Optional<Blocage> findByBloqueurIdAndBloqueId(Long bloqueurId, Long bloqueId);
    List<Blocage> findByBloqueurIdOrderByDateCreationDesc(Long bloqueurId);

    /** Vrai si l'un des deux a bloqué l'autre. */
    default boolean entre(Long a, Long b) {
        return existsByBloqueurIdAndBloqueId(a, b) || existsByBloqueurIdAndBloqueId(b, a);
    }
}
