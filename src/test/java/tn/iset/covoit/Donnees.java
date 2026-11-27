package tn.iset.covoit;

import tn.iset.covoit.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Petits objets de test prêts à l'emploi. */
public final class Donnees {

    private Donnees() {
    }

    public static Utilisateur utilisateur(long id, String prenom, Role role) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setPrenom(prenom);
        u.setNom("Test");
        u.setEmail(prenom.toLowerCase() + "@iset.tn");
        u.setRole(role);
        return u;
    }

    /** Trajet qui part dans « dansHeures » heures (négatif = déjà parti). */
    public static Trajet trajet(long id, Utilisateur conducteur, long dansHeures, int placesLibres) {
        LocalDateTime depart = LocalDateTime.now().plusHours(dansHeures);
        Trajet t = new Trajet();
        t.setId(id);
        t.setConducteur(conducteur);
        t.setVilleDepart("Tunis");
        t.setVilleArrivee("ISET Zaghouan");
        t.setDateDepart(depart.toLocalDate());
        t.setHeureDepart(depart.toLocalTime().withSecond(0).withNano(0));
        t.setPlacesTotales(4);
        t.setPlacesDisponibles(placesLibres);
        t.setPrix(6.0);
        return t;
    }

    public static Trajet trajetLe(long id, Utilisateur conducteur, LocalDate date, LocalTime heure, int placesLibres) {
        Trajet t = trajet(id, conducteur, 48, placesLibres);
        t.setDateDepart(date);
        t.setHeureDepart(heure);
        return t;
    }

    public static Reservation reservation(long id, Trajet trajet, Utilisateur passager, int places, StatutReservation statut) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setTrajet(trajet);
        r.setPassager(passager);
        r.setNombrePlaces(places);
        r.setStatut(statut);
        return r;
    }
}
