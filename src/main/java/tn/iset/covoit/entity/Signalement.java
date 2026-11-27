package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Un utilisateur signale un comportement à l'administration. */
@Entity
@Table(name = "signalements")
public class Signalement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "signale_id")
    private Utilisateur signale;

    @ManyToOne
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30)")
    private MotifSignalement motif;

    @Column(length = 1000)
    private String details;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    /** Traité par un administrateur. */
    @Column(nullable = false)
    private boolean traite;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Utilisateur getAuteur() { return auteur; }
    public void setAuteur(Utilisateur auteur) { this.auteur = auteur; }
    public Utilisateur getSignale() { return signale; }
    public void setSignale(Utilisateur signale) { this.signale = signale; }
    public Trajet getTrajet() { return trajet; }
    public void setTrajet(Trajet trajet) { this.trajet = trajet; }
    public MotifSignalement getMotif() { return motif; }
    public void setMotif(MotifSignalement motif) { this.motif = motif; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public boolean isTraite() { return traite; }
    public void setTraite(boolean traite) { this.traite = traite; }
    public LocalDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDateTime dateTraitement) { this.dateTraitement = dateTraitement; }
}
