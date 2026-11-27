package tn.iset.covoit.dto;

import java.util.List;

/** Réponse de l'assistant : un texte, éventuellement des trajets trouvés et des suggestions de questions. */
public record AssistantResponse(String reponse, List<TrajetResponse> trajets, List<String> suggestions) {
}
