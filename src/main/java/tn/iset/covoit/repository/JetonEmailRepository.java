package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.iset.covoit.entity.JetonEmail;

import java.util.Optional;

public interface JetonEmailRepository extends JpaRepository<JetonEmail, Long> {
    Optional<JetonEmail> findByEmpreinte(String empreinte);
}
