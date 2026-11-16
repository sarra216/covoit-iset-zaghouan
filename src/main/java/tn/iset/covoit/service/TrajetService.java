package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.RoutePopulaire;
import tn.iset.covoit.dto.TrajetRequest;
import tn.iset.covoit.dto.TrajetResponse;
import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.Trajet;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.List;

/** User stories #26 : Proposer un trajet  et  #3 : Chercher un trajet */
@Service
public class TrajetService {

    private final TrajetRepository trajetRepository;
    private final UtilisateurRepository utilisateurRepository;

    public TrajetService(TrajetRepository trajetRepository, UtilisateurRepository utilisateurRepository) {
        this.trajetRepository = trajetRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** #26 : un conducteur publie un trajet. */
    @Transactional
    public TrajetResponse proposer(TrajetRequest requete) {
        Utilisateur conducteur = utilisateurRepository.findById(requete.conducteurId())
                .orElseThrow(() -> ApiException.introuvable("Conducteur introuvable"));

        if (conducteur.getRole() != Role.CONDUCTEUR) {
            throw ApiException.interdit("Seul un conducteur peut proposer un trajet");
        }
        if (!conducteur.aVehicule()) {
            throw ApiException.requeteInvalide(
                    "Veuillez enregistrer les détails de votre véhicule avant de proposer un trajet");
        }
        if (requete.villeDepart().trim().equalsIgnoreCase(requete.villeArrivee().trim())) {
            throw ApiException.requeteInvalide("Le départ et l'arrivée doivent être différents");
        }

        Trajet trajet = new Trajet();
        trajet.setConducteur(conducteur);
        trajet.setVilleDepart(requete.villeDepart().trim());
        trajet.setVilleArrivee(requete.villeArrivee().trim());
        trajet.setDateDepart(requete.dateDepart());
        trajet.setHeureDepart(requete.heureDepart());
        trajet.setPlacesTotales(requete.places());
        trajet.setPlacesDisponibles(requete.places());
        trajet.setPrix(requete.prix());
        if (requete.lieuDepart() != null && !requete.lieuDepart().isBlank()) {
            trajet.setLieuDepart(requete.lieuDepart().trim());
        }
        trajet.setLatitude(requete.latitude());
        trajet.setLongitude(requete.longitude());

        if (trajet.estPasse()) {
            throw ApiException.requeteInvalide("L'heure de départ est déjà passée");
        }
        return TrajetResponse.from(trajetRepository.save(trajet));
    }

    /** #3 : un passager cherche un trajet (tous les critères sont facultatifs). */
    @Transactional(readOnly = true)
    public List<TrajetResponse> rechercher(String depart, String arrivee, LocalDate date) {
        return trajetRepository.rechercher(
                        depart == null ? "" : depart.trim(),
                        arrivee == null ? "" : arrivee.trim(),
                        date,
                        LocalDate.now())
                .stream()
                .filter(t -> !t.estPasse()) // retire les trajets d'aujourd'hui déjà partis
                .map(TrajetResponse::from)
                .toList();
    }

    /** Détail d'un trajet (page « Détails du trajet »). */
    @Transactional(readOnly = true)
    public TrajetResponse detail(Long id) {
        return trajetRepository.findById(id)
                .map(TrajetResponse::from)
                .orElseThrow(() -> ApiException.introuvable("Trajet introuvable"));
    }

    /** Les 5 itinéraires les plus proposés (accueil, « Trajets populaires »). */
    @Transactional(readOnly = true)
    public List<RoutePopulaire> routesPopulaires() {
        return trajetRepository.routesPopulaires(LocalDate.now()).stream().limit(5).toList();
    }
}
