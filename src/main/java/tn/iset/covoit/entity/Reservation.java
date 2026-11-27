package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "passager_id")
    private Utilisateur passager;

    @Column(name = "nombre_places", nullable = false)
    private int nombrePlaces;

    @Enumerated(EnumType.STRING)
    // varchar plutôt qu'un type ENUM MySQL : on peut ajouter des statuts sans casser la table
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    @Column(name = "date_reservation", nullable = false)
    private LocalDateTime dateReservation = LocalDateTime.now();

    @Column(name = "date_annulation")
    private LocalDateTime dateAnnulation;

    /** Date à laquelle le conducteur a accepté ou refusé la demande. */
    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    /** Vrai quand le rappel « départ dans moins de 24 h » a été envoyé (#9). */
    @Column(name = "rappel_envoye")
    private Boolean rappelEnvoye;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trajet getTrajet() { return trajet; }
    public void setTrajet(Trajet trajet) { this.trajet = trajet; }

    public Utilisateur getPassager() { return passager; }
    public void setPassager(Utilisateur passager) { this.passager = passager; }

    public int getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(int nombrePlaces) { this.nombrePlaces = nombrePlaces; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    public LocalDateTime getDateAnnulation() { return dateAnnulation; }
    public void setDateAnnulation(LocalDateTime dateAnnulation) { this.dateAnnulation = dateAnnulation; }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }

    public boolean isRappelEnvoye() { return Boolean.TRUE.equals(rappelEnvoye); }
    public void setRappelEnvoye(boolean rappelEnvoye) { this.rappelEnvoye = rappelEnvoye; }

    /** Une réservation « active » bloque des places : en attente ou confirmée. */
    public boolean estActive() {
        return statut == StatutReservation.EN_ATTENTE || statut == StatutReservation.CONFIRMEE;
    }
}
