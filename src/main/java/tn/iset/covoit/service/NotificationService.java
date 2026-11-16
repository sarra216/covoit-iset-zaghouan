package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.NotificationResponse;
import tn.iset.covoit.entity.Notification;
import tn.iset.covoit.entity.TypeNotification;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.repository.NotificationRepository;

import java.util.List;

/** User story #9 : notifications dans l'application (cloche). */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** Appelé par les autres services (réservation, décision, message, rappel, avis). */
    @Transactional
    public void notifier(Utilisateur destinataire, TypeNotification type, String titre, String message, String lien) {
        Notification n = new Notification();
        n.setDestinataire(destinataire);
        n.setType(type);
        n.setTitre(titre);
        n.setMessage(message.length() > 300 ? message.substring(0, 297) + "..." : message);
        n.setLien(lien);
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> lister(Long utilisateurId) {
        return notificationRepository.findTop30ByDestinataireIdOrderByDateCreationDesc(utilisateurId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long nombreNonLues(Long utilisateurId) {
        return notificationRepository.countByDestinataireIdAndLueFalse(utilisateurId);
    }

    @Transactional
    public void toutMarquerLu(Long utilisateurId) {
        notificationRepository.findByDestinataireIdAndLueFalse(utilisateurId).forEach(n -> n.setLue(true));
    }
}
