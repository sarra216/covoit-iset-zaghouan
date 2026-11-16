package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ProfilRequest;
import tn.iset.covoit.dto.UtilisateurResponse;
import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.time.LocalDate;

/** Compléter le profil : identité, mode conducteur et véhicule. */
@Service
public class ProfilService {

    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;

    public ProfilService(UtilisateurRepository utilisateurRepository, TrajetRepository trajetRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
    }

    @Transactional
    public UtilisateurResponse mettreAJour(Long id, ProfilRequest requete) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> ApiException.introuvable("Utilisateur introuvable"));

        utilisateur.setPrenom(requete.prenom().trim());
        utilisateur.setNom(requete.nom().trim());
        utilisateur.setTelephone(requete.telephone());

        if (requete.conducteur()) {
            if (vide(requete.voitureModele()) || vide(requete.voitureCouleur()) || vide(requete.matricule())) {
                throw ApiException.requeteInvalide(
                        "Veuillez renseigner le modèle, la couleur et la matricule de votre voiture");
            }
            utilisateur.setRole(Role.CONDUCTEUR);
            utilisateur.setVoitureModele(requete.voitureModele().trim());
            utilisateur.setVoitureCouleur(requete.voitureCouleur().trim());
            utilisateur.setMatricule(requete.matricule().trim());
        } else {
            boolean aDesTrajetsAVenir = !trajetRepository
                    .findByConducteurIdAndDateDepartGreaterThanEqualOrderByDateDepartAscHeureDepartAsc(id, LocalDate.now())
                    .isEmpty();
            if (utilisateur.getRole() == Role.CONDUCTEUR && aDesTrajetsAVenir) {
                throw ApiException.conflit("Vous avez des trajets à venir : impossible de désactiver le mode conducteur");
            }
            utilisateur.setRole(Role.PASSAGER);
        }
        return UtilisateurResponse.from(utilisateur);
    }

    private static boolean vide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
