package tn.iset.covoit.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tn.iset.covoit.exception.ApiException;

/**
 * Donne l'id de l'utilisateur connecté (lu dans son jeton) et vérifie
 * qu'il n'agit pas au nom de quelqu'un d'autre.
 */
@Component
public class UtilisateurCourant {

    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long id)) {
            throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Veuillez vous connecter");
        }
        return id;
    }

    /** Refuse la requête si l'id envoyé n'est pas celui de l'utilisateur connecté. */
    public void verifier(Long idDemande) {
        if (idDemande == null || !idDemande.equals(id())) {
            throw ApiException.interdit("Action non autorisée pour ce compte");
        }
    }

    /** L'utilisateur connecté doit être l'un des deux. */
    public void verifierUnDe(Long id1, Long id2) {
        Long moi = id();
        if (!moi.equals(id1) && !moi.equals(id2)) {
            throw ApiException.interdit("Cette conversation ne vous concerne pas");
        }
    }
}
