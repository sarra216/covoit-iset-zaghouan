package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** User story #8 : avis d'un passager sur un conducteur, après un trajet. */
@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Une seule note par réservation. */
    @OneToOne(optional = false)
    @JoinColumn(name = "reservation_id", unique = true)
    private Reservation reservation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conducteur_id")
    private Utilisateur conducteur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "passager_id")
    private Utilisateur passager;

    /** De 1 à 5 étoiles. */
    @Column(nullable = false)
    private int note;

    @Column(length = 500)
    private String commentaire;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public Utilisateur getConducteur() { return conducteur; }
    public void setConducteur(Utilisateur conducteur) { this.conducteur = conducteur; }

    public Utilisateur getPassager() { return passager; }
    public void setPassager(Utilisateur passager) { this.passager = passager; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
