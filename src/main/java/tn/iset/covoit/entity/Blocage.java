package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** « bloqueur » ne veut plus de contact avec « bloque » : plus de messages ni de réservations entre eux. */
@Entity
@Table(name = "blocages", uniqueConstraints = @UniqueConstraint(columnNames = {"bloqueur_id", "bloque_id"}))
public class Blocage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bloqueur_id")
    private Utilisateur bloqueur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bloque_id")
    private Utilisateur bloque;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Long getId() { return id; }
    public Utilisateur getBloqueur() { return bloqueur; }
    public void setBloqueur(Utilisateur bloqueur) { this.bloqueur = bloqueur; }
    public Utilisateur getBloque() { return bloque; }
    public void setBloque(Utilisateur bloque) { this.bloque = bloque; }
    public LocalDateTime getDateCreation() { return dateCreation; }
}
