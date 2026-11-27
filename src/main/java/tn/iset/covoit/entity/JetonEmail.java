package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Lien à usage unique envoyé par email (vérification du compte ou nouveau mot de passe).
 * On ne stocke que l'empreinte SHA-256 du jeton : une fuite de la base ne permet pas de s'en servir.
 */
@Entity
@Table(name = "jetons_email")
public class JetonEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, columnDefinition = "varchar(40)")
    private TypeJeton type;

    @Column(nullable = false, unique = true, length = 64)
    private String empreinte;

    @Column(nullable = false)
    private LocalDateTime expiration;

    @Column(nullable = false)
    private boolean utilise;

    public Long getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public TypeJeton getType() { return type; }
    public void setType(TypeJeton type) { this.type = type; }
    public String getEmpreinte() { return empreinte; }
    public void setEmpreinte(String empreinte) { this.empreinte = empreinte; }
    public LocalDateTime getExpiration() { return expiration; }
    public void setExpiration(LocalDateTime expiration) { this.expiration = expiration; }
    public boolean isUtilise() { return utilise; }
    public void setUtilise(boolean utilise) { this.utilise = utilise; }
}
