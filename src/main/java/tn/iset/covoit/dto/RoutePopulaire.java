package tn.iset.covoit.dto;

/** Itinéraire le plus proposé : nombre de trajets à venir et prix le plus bas. */
public record RoutePopulaire(String villeDepart, String villeArrivee, Long nombreTrajets, Double prixMin) {
}
