package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ProfilConducteurResponse;
import tn.iset.covoit.dto.TrajetResponse;
import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.List;

/** User story #2 : Consulter le profil du conducteur */
@Service
public class ConducteurService {

    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;

    public ConducteurService(UtilisateurRepository utilisateurRepository, TrajetRepository trajetRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
    }

    @Transactional(readOnly = true)
    public ProfilConducteurResponse profil(Long id) {
        Utilisateur conducteur = utilisateurRepository.findById(id)
                .filter(u -> u.getRole() == Role.CONDUCTEUR)
                .orElseThrow(() -> ApiException.introuvable("Conducteur introuvable"));

        List<TrajetResponse> trajetsAVenir = trajetRepository
                .findByConducteurIdAndDateDepartGreaterThanEqualOrderByDateDepartAscHeureDepartAsc(id, LocalDate.now())
                .stream()
                .filter(t -> !t.estPasse() && !t.isAnnule())
                .map(TrajetResponse::from)
                .toList();

        return new ProfilConducteurResponse(
                conducteur.getId(),
                conducteur.getNom(),
                conducteur.getPrenom(),
                conducteur.getDateInscription().toLocalDate(),
                trajetRepository.countByConducteurId(id),
                conducteur.getVoitureModele(),
                conducteur.getVoitureCouleur(),
                conducteur.getNoteMoyenne(),
                conducteur.getNombreAvis(),
                trajetsAVenir);
    }
}
