package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.MessageRequest;
import tn.iset.covoit.dto.MessageResponse;
import tn.iset.covoit.entity.Message;
import tn.iset.covoit.entity.TypeNotification;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.BlocageRepository;
import tn.iset.covoit.repository.MessageRepository;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.util.List;

/** User story #7 : Contacter le conducteur */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;
    private final NotificationService notificationService;
    private final BlocageRepository blocageRepository;

    public MessageService(MessageRepository messageRepository, UtilisateurRepository utilisateurRepository,
                          TrajetRepository trajetRepository, NotificationService notificationService,
                          BlocageRepository blocageRepository) {
        this.blocageRepository = blocageRepository;
        this.messageRepository = messageRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public MessageResponse envoyer(MessageRequest requete) {
        if (requete.expediteurId().equals(requete.destinataireId())) {
            throw ApiException.requeteInvalide("Vous ne pouvez pas vous envoyer un message");
        }
        Utilisateur expediteur = utilisateurRepository.findById(requete.expediteurId())
                .orElseThrow(() -> ApiException.introuvable("Expéditeur introuvable"));
        Utilisateur destinataire = utilisateurRepository.findById(requete.destinataireId())
                .orElseThrow(() -> ApiException.introuvable("Destinataire introuvable"));

        if (blocageRepository.entre(expediteur.getId(), destinataire.getId())) {
            throw ApiException.interdit("Vous ne pouvez pas écrire à cet utilisateur");
        }
        if (destinataire.isSuspendu()) {
            throw ApiException.requeteInvalide("Ce compte n'est plus actif");
        }

        Message message = new Message();
        message.setExpediteur(expediteur);
        message.setDestinataire(destinataire);
        message.setContenu(requete.contenu().trim());
        if (requete.trajetId() != null) {
            message.setTrajet(trajetRepository.findById(requete.trajetId())
                    .orElseThrow(() -> ApiException.introuvable("Trajet introuvable")));
        }
        message = messageRepository.save(message);
        notificationService.notifier(destinataire, TypeNotification.NOUVEAU_MESSAGE,
                "Nouveau message de " + expediteur.getPrenom(),
                message.getContenu(),
                "messages.html?avec=" + expediteur.getId());
        return MessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> conversation(Long utilisateur1, Long utilisateur2) {
        return messageRepository.conversation(utilisateur1, utilisateur2)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> messagesDe(Long utilisateurId) {
        return messageRepository.messagesDe(utilisateurId)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }
}
