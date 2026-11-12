package tn.iset.covoit.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.ConnexionRequest;
import tn.iset.covoit.dto.InscriptionRequest;
import tn.iset.covoit.dto.UtilisateurResponse;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.UtilisateurRepository;

/** User story #1 : S'inscrire / Se connecter */
@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UtilisateurResponse inscrire(InscriptionRequest requete) {
        String email = requete.email().trim().toLowerCase();
        if (utilisateurRepository.existsByEmail(email)) {
            throw ApiException.conflit("Un compte existe déjà avec cet email");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(requete.nom().trim());
        utilisateur.setPrenom(requete.prenom().trim());
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(passwordEncoder.encode(requete.motDePasse()));
        utilisateur.setTelephone(requete.telephone());
        utilisateur.setRole(requete.role());

        return UtilisateurResponse.from(utilisateurRepository.save(utilisateur));
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse connecter(ConnexionRequest requete) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(requete.email().trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(requete.motDePasse(), u.getMotDePasse()))
                // Même message dans les deux cas : on ne révèle pas si l'email existe
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"));

        return UtilisateurResponse.from(utilisateur);
    }
}
