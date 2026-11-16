package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Notification;
import tn.iset.covoit.entity.TypeNotification;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, TypeNotification type, String titre, String message, String lien,
                                   boolean lue, LocalDateTime dateCreation) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getTitre(), n.getMessage(), n.getLien(),
                n.isLue(), n.getDateCreation());
    }
}
