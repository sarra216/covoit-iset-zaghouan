package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iset.covoit.entity.Signalement;

import java.util.List;

public interface SignalementRepository extends JpaRepository<Signalement, Long> {
    List<Signalement> findByTraiteOrderByDateCreationDesc(boolean traite);
    boolean existsByAuteurIdAndSignaleIdAndTraiteFalse(Long auteurId, Long signaleId);
    long countByTraiteFalse();
    long countBySignaleId(Long signaleId);
}
