package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expediteur_id")
    private Utilisateur expediteur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinataire_id")
    private Utilisateur destinataire;

    /** Trajet concerné par le message (facultatif). */
    @ManyToOne
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    @Column(nullable = false, length = 500)
    private String contenu;

    @Column(name = "date_envoi", nullable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Utilisateur getExpediteur() { return expediteur; }
    public void setExpediteur(Utilisateur expediteur) { this.expediteur = expediteur; }

    public Utilisateur getDestinataire() { return destinataire; }
    public void setDestinataire(Utilisateur destinataire) { this.destinataire = destinataire; }

    public Trajet getTrajet() { return trajet; }
    public void setTrajet(Trajet trajet) { this.trajet = trajet; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
}
