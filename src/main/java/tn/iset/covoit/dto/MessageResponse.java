package tn.iset.covoit.dto;

import tn.iset.covoit.entity.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long expediteurId,
        String expediteurNom,
        Long destinataireId,
        String destinataireNom,
        Long trajetId,
        String contenu,
        LocalDateTime dateEnvoi
) {

    public static MessageResponse from(Message m) {
        return new MessageResponse(m.getId(),
                m.getExpediteur().getId(), m.getExpediteur().getNomComplet(),
                m.getDestinataire().getId(), m.getDestinataire().getNomComplet(),
                m.getTrajet() == null ? null : m.getTrajet().getId(),
                m.getContenu(), m.getDateEnvoi());
    }
}
