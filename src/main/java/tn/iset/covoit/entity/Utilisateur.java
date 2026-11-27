package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Mot de passe chiffré avec BCrypt, jamais en clair. */
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(nullable = false, length = 8)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private Role role;

    /** null pour les comptes créés avant la vérification d'email : considérés comme vérifiés. */
    @Column(name = "email_verifie")
    private Boolean emailVerifie;

    /** Compte suspendu par un administrateur (ou supprimé par son propriétaire). */
    private Boolean suspendu;

    /** Compte supprimé à la demande de l'utilisateur : données personnelles effacées. */
    private Boolean supprime;

    /** Date à laquelle l'utilisateur a accepté les conditions d'utilisation. */
    @Column(name = "date_acceptation_conditions")
    private LocalDateTime dateAcceptationConditions;

    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription = LocalDateTime.now();

    /** Véhicule du conducteur (vide pour un passager). */
    @Column(name = "voiture_modele", length = 60)
    private String voitureModele;

    @Column(name = "voiture_couleur", length = 30)
    private String voitureCouleur;

    @Column(length = 20)
    private String matricule;

    /** Note moyenne reçue comme conducteur (#8), mise à jour à chaque nouvel avis. */
    @Column(name = "note_moyenne")
    private Double noteMoyenne;

    @Column(name = "nombre_avis")
    private Integer nombreAvis;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getVoitureModele() { return voitureModele; }
    public void setVoitureModele(String voitureModele) { this.voitureModele = voitureModele; }

    public String getVoitureCouleur() { return voitureCouleur; }
    public void setVoitureCouleur(String voitureCouleur) { this.voitureCouleur = voitureCouleur; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public Double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(Double noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public int getNombreAvis() { return nombreAvis == null ? 0 : nombreAvis; }
    public void setNombreAvis(int nombreAvis) { this.nombreAvis = nombreAvis; }

    /** Vrai si le véhicule est renseigné (obligatoire pour proposer un trajet). */
    public boolean aVehicule() {
        return estRempli(voitureModele) && estRempli(voitureCouleur) && estRempli(matricule);
    }

    private static boolean estRempli(String valeur) {
        return valeur != null && !valeur.isBlank();
    }

    public boolean isEmailVerifie() { return !Boolean.FALSE.equals(emailVerifie); }
    public void setEmailVerifie(boolean emailVerifie) { this.emailVerifie = emailVerifie; }

    public boolean isSuspendu() { return Boolean.TRUE.equals(suspendu); }
    public void setSuspendu(boolean suspendu) { this.suspendu = suspendu; }

    public boolean isSupprime() { return Boolean.TRUE.equals(supprime); }
    public void setSupprime(boolean supprime) { this.supprime = supprime; }

    public LocalDateTime getDateAcceptationConditions() { return dateAcceptationConditions; }
    public void setDateAcceptationConditions(LocalDateTime d) { this.dateAcceptationConditions = d; }

    public boolean estAdmin() { return role == Role.ADMIN; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
