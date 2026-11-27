package tn.iset.covoit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoie les emails (vérification du compte, mot de passe oublié).
 * Si aucun serveur SMTP n'est configuré (spring.mail.host vide, cas du développement),
 * l'email est écrit dans la console : on peut copier le lien depuis les logs d'Eclipse.
 */
@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> expediteurMail;
    private final String hoteSmtp;
    private final String adresseExpediteur;

    public EmailService(ObjectProvider<JavaMailSender> expediteurMail,
                        @Value("${spring.mail.host:}") String hoteSmtp,
                        @Value("${app.mail.expediteur:no-reply@covoit-iset.tn}") String adresseExpediteur) {
        this.expediteurMail = expediteurMail;
        this.hoteSmtp = hoteSmtp;
        this.adresseExpediteur = adresseExpediteur;
    }

    public void envoyer(String destinataire, String sujet, String texte) {
        JavaMailSender mail = expediteurMail.getIfAvailable();
        if (hoteSmtp == null || hoteSmtp.isBlank() || mail == null) {
            LOG.info("""

                    ===== EMAIL (SMTP non configuré : affiché ici au lieu d'être envoyé) =====
                    À : {}
                    Sujet : {}

                    {}
                    =========================================================================""", destinataire, sujet, texte);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(adresseExpediteur);
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(texte);
            mail.send(message);
            LOG.info("Email « {} » envoyé à {}", sujet, destinataire);
        } catch (Exception e) {
            // Un email qui échoue ne doit pas faire échouer l'inscription : on le note dans les logs
            LOG.error("Échec de l'envoi de l'email « {} » à {} : {}", sujet, destinataire, e.getMessage());
        }
    }
}
