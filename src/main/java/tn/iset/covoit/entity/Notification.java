package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinataire_id")
    private Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    // varchar plutôt qu'un type ENUM MySQL : on peut ajouter des types sans casser la table
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30)")
    private TypeNotification type;

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(nullable = false, length = 300)
    private String message;

    /** Page à ouvrir quand on clique sur la notification. */
    @Column(length = 200)
    private String lien;

    @Column(nullable = false)
    private boolean lue = false;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Utilisateur getDestinataire() { return destinataire; }
    public void setDestinataire(Utilisateur destinataire) { this.destinataire = destinataire; }

    public TypeNotification getType() { return type; }
    public void setType(TypeNotification type) { this.type = type; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }

    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
