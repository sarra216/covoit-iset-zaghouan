package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.BlocageResponse;
import tn.iset.covoit.entity.Blocage;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.BlocageRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.util.List;

/** Bloquer un utilisateur : plus de messages ni de réservations entre les deux. */
@Service
public class BlocageService {

    private final BlocageRepository blocageRepository;
    private final UtilisateurRepository utilisateurRepository;

    public BlocageService(BlocageRepository blocageRepository, UtilisateurRepository utilisateurRepository) {
        this.blocageRepository = blocageRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public void bloquer(Long bloqueurId, Long bloqueId) {
        if (bloqueurId.equals(bloqueId)) {
            throw ApiException.requeteInvalide("Vous ne pouvez pas vous bloquer vous-même");
        }
        if (blocageRepository.existsByBloqueurIdAndBloqueId(bloqueurId, bloqueId)) return;
        Utilisateur bloqueur = utilisateurRepository.findById(bloqueurId)
                .orElseThrow(() -> ApiException.introuvable("Utilisateur introuvable"));
        Utilisateur bloque = utilisateurRepository.findById(bloqueId)
                .orElseThrow(() -> ApiException.introuvable("Utilisateur introuvable"));
        Blocage b = new Blocage();
        b.setBloqueur(bloqueur);
        b.setBloque(bloque);
        blocageRepository.save(b);
    }

    @Transactional
    public void debloquer(Long bloqueurId, Long bloqueId) {
        blocageRepository.findByBloqueurIdAndBloqueId(bloqueurId, bloqueId).ifPresent(blocageRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<BlocageResponse> liste(Long bloqueurId) {
        return blocageRepository.findByBloqueurIdOrderByDateCreationDesc(bloqueurId).stream()
                .map(BlocageResponse::from)
                .toList();
    }
}
