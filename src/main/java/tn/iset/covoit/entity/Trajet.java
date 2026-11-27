package tn.iset.covoit.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "trajets")
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conducteur_id")
    private Utilisateur conducteur;

    @Column(name = "ville_depart", nullable = false, length = 80)
    private String villeDepart;

    @Column(name = "ville_arrivee", nullable = false, length = 80)
    private String villeArrivee;

    @Column(name = "date_depart", nullable = false)
    private LocalDate dateDepart;

    @Column(name = "heure_depart", nullable = false)
    private LocalTime heureDepart;

    @Column(name = "places_totales", nullable = false)
    private int placesTotales;

    @Column(name = "places_disponibles", nullable = false)
    private int placesDisponibles;

    /** Point de rendez-vous précis, choisi sur la carte (facultatif). */
    @Column(name = "lieu_depart", length = 200)
    private String lieuDepart;

    private Double latitude;

    private Double longitude;

    /** Prix par place, en dinars. */
    @Column(nullable = false)
    private double prix;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    /** Trajet annulé par son conducteur (null = non annulé, pour les anciennes lignes). */
    private Boolean annule;

    /** Verrou optimiste : empêche deux réservations simultanées de la même place. */
    @Version
    private Long version;

    public boolean isAnnule() { return Boolean.TRUE.equals(annule); }
    public void setAnnule(boolean annule) { this.annule = annule; }

    public boolean estPasse() {
        LocalDateTime depart = LocalDateTime.of(dateDepart, heureDepart);
        return depart.isBefore(LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Utilisateur getConducteur() { return conducteur; }
    public void setConducteur(Utilisateur conducteur) { this.conducteur = conducteur; }

    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String villeDepart) { this.villeDepart = villeDepart; }

    public String getVilleArrivee() { return villeArrivee; }
    public void setVilleArrivee(String villeArrivee) { this.villeArrivee = villeArrivee; }

    public LocalDate getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDate dateDepart) { this.dateDepart = dateDepart; }

    public LocalTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalTime heureDepart) { this.heureDepart = heureDepart; }

    public int getPlacesTotales() { return placesTotales; }
    public void setPlacesTotales(int placesTotales) { this.placesTotales = placesTotales; }

    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }

    public String getLieuDepart() { return lieuDepart; }
    public void setLieuDepart(String lieuDepart) { this.lieuDepart = lieuDepart; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
