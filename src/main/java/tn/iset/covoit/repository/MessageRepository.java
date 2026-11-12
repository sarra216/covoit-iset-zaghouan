package tn.iset.covoit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iset.covoit.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Tous les messages échangés entre deux utilisateurs, du plus ancien au plus récent. */
    @Query("""
            SELECT m FROM Message m
            WHERE (m.expediteur.id = :a AND m.destinataire.id = :b)
               OR (m.expediteur.id = :b AND m.destinataire.id = :a)
            ORDER BY m.dateEnvoi
            """)
    List<Message> conversation(@Param("a") Long utilisateur1, @Param("b") Long utilisateur2);

    /** Tous les messages envoyés ou reçus par un utilisateur, du plus récent au plus ancien. */
    @Query("""
            SELECT m FROM Message m
            WHERE m.expediteur.id = :id OR m.destinataire.id = :id
            ORDER BY m.dateEnvoi DESC
            """)
    List<Message> messagesDe(@Param("id") Long utilisateurId);
}
