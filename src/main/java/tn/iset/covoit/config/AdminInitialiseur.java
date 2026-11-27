package tn.iset.covoit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.entity.Role;
import tn.iset.covoit.repository.UtilisateurRepository;

/**
 * Au démarrage, donne le rôle ADMIN au compte dont l'email est dans « app.admin.email ».
 * Procédure : créer un compte normal avec cet email, puis redémarrer l'application.
 */
@Component
public class AdminInitialiseur implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInitialiseur.class);

    private final UtilisateurRepository utilisateurRepository;
    private final String emailAdmin;

    public AdminInitialiseur(UtilisateurRepository utilisateurRepository, @Value("${app.admin.email:}") String emailAdmin) {
        this.utilisateurRepository = utilisateurRepository;
        this.emailAdmin = emailAdmin.trim().toLowerCase();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (emailAdmin.isEmpty()) return;
        utilisateurRepository.findByEmail(emailAdmin).ifPresentOrElse(u -> {
            if (u.getRole() != Role.ADMIN) {
                u.setRole(Role.ADMIN);
                u.setEmailVerifie(true);
                utilisateurRepository.save(u);
                LOG.warn("Le compte {} est maintenant administrateur", emailAdmin);
            }
        }, () -> LOG.warn("app.admin.email = {} : aucun compte avec cet email. Inscrivez-vous puis redémarrez.", emailAdmin));
    }
}
