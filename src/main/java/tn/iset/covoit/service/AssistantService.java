package tn.iset.covoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.covoit.dto.AssistantRequest;
import tn.iset.covoit.dto.AssistantResponse;
import tn.iset.covoit.dto.ReservationResponse;
import tn.iset.covoit.dto.TrajetResponse;
import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.StatutReservation;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.TrajetRepository;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User story #10 : assistant de l'application (chatbot).
 * Il comprend des questions simples en français, sans service externe ni clé d'API :
 * recherche de trajets (« un trajet de Tunis demain matin ? »), réservations de l'utilisateur,
 * demandes en attente du conducteur, et questions fréquentes (annuler, proposer, payer…).
 */
@Service
public class AssistantService {

    private static final List<String> VILLES_CONNUES = List.of("ISET Zaghouan", "Zaghouan", "Tunis", "Ben Arous",
            "Ariana", "La Manouba", "El Fahs", "Zriba", "Bir Mcherga", "Nadhour", "Saouaf", "Mornag", "Mohamedia",
            "Hammam Lif", "Hammam Zriba", "Nabeul", "Hammamet", "Enfidha", "Sousse", "Grombalia", "Bou Argoub",
            "Megrine", "Rades", "Fouchana");
    /** Mots qui désignent l'institut. */
    private static final List<String> ALIAS_ISET = List.of("iset zaghouan", "l iset", "iset", "institut");
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH);
    private static final DateTimeFormatter COURT = DateTimeFormatter.ofPattern("dd/MM");

    private final UtilisateurRepository utilisateurRepository;
    private final TrajetRepository trajetRepository;
    private final TrajetService trajetService;
    private final ReservationService reservationService;
    private final DecisionService decisionService;

    public AssistantService(UtilisateurRepository utilisateurRepository, TrajetRepository trajetRepository,
                            TrajetService trajetService, ReservationService reservationService,
                            DecisionService decisionService) {
        this.utilisateurRepository = utilisateurRepository;
        this.trajetRepository = trajetRepository;
        this.trajetService = trajetService;
        this.reservationService = reservationService;
        this.decisionService = decisionService;
    }

    @Transactional(readOnly = true)
    public AssistantResponse repondre(AssistantRequest requete) {
        Utilisateur u = utilisateurRepository.findById(requete.utilisateurId())
                .orElseThrow(() -> ApiException.introuvable("Utilisateur introuvable"));
        boolean conducteur = u.getRole() == Role.CONDUCTEUR;
        String texte = normaliser(requete.message());
        List<Ville> villes = trouverVilles(texte);

        if (texte.matches("^(bonjour|salut|salam|aslema|bonsoir|hello|coucou|ahla)\\b.*") && texte.length() < 30 && villes.isEmpty()) {
            return reponse("Bonjour " + u.getPrenom() + " ! Je peux chercher un trajet pour vous, afficher vos réservations "
                    + "ou répondre à vos questions sur l'application.", suggestions(conducteur));
        }
        if (contient(texte, "merci", "chokran", "yaatik")) {
            return reponse("Avec plaisir ! Bon trajet vers l'ISET.", suggestions(conducteur));
        }
        if (contient(texte, "mes reservations", "ma reservation", "mes trajets", "j ai reserve", "mes places")) {
            return mesReservations(u, conducteur);
        }
        if (conducteur && contient(texte, "demande", "en attente", "accepter", "refuser") && villes.isEmpty()) {
            return mesDemandes(u, conducteur);
        }
        if (villes.isEmpty()) {
            AssistantResponse faq = questionFrequente(texte, conducteur);
            if (faq != null) return faq;
        }
        LocalDate date = trouverDate(texte);
        if (!villes.isEmpty() || date != null || contient(texte, "trajet", "aller", "partir", "voiture", "place", "covoit", "rentrer")) {
            return rechercher(texte, villes, date, conducteur);
        }
        return reponse("Je n'ai pas compris. Essayez par exemple « un trajet de Tunis vers l'ISET demain matin » "
                + "ou « comment annuler une réservation ? ».", suggestions(conducteur));
    }

    // ---------- Recherche de trajets ----------

    private AssistantResponse rechercher(String texte, List<Ville> villes, LocalDate date, boolean conducteur) {
        String depart = null, arrivee = null;
        List<Ville> sansRole = new ArrayList<>();
        for (Ville v : villes) {
            String avant = texte.substring(Math.max(0, v.debut - 14), v.debut);
            if (depart == null && avant.matches(".*\\b(de|depuis|du|d)\\s*$")) depart = v.nom;
            else if (arrivee == null && avant.matches(".*\\b(a|au|vers|pour|jusqu a|direction)\\s*$")) arrivee = v.nom;
            else sansRole.add(v);
        }
        for (Ville v : sansRole) {
            if (depart == null && arrivee == null && sansRole.size() == 1) {
                if (v.iset) arrivee = v.nom; else depart = v.nom;
            } else if (depart == null) depart = v.nom;
            else if (arrivee == null) arrivee = v.nom;
        }

        LocalTime min = null, max = null;
        Matcher h = Pattern.compile("\\b(\\d{1,2})\\s*h\\s*(\\d{2})?").matcher(texte);
        if (h.find()) {
            int heure = Math.min(23, Integer.parseInt(h.group(1)));
            min = LocalTime.of(Math.max(0, heure - 1), 0);
            max = LocalTime.of(Math.min(23, heure + 1), 59);
        } else if (contient(texte, "matin")) {
            max = LocalTime.of(11, 59);
        } else if (contient(texte, "apres midi", "soir", "retour")) {
            min = LocalTime.NOON;
        }

        final LocalTime hMin = min, hMax = max;
        List<TrajetResponse> trajets = trajetService.rechercher(depart, arrivee, date).stream()
                .filter(t -> hMin == null || !t.heureDepart().isBefore(hMin))
                .filter(t -> hMax == null || !t.heureDepart().isAfter(hMax))
                .limit(5)
                .toList();

        String criteres = (depart != null ? " de " + depart : "") + (arrivee != null ? " vers " + arrivee : "")
                + (date != null ? " " + decrireDate(date) : "")
                + (hMin != null && hMax != null ? " vers " + texte.substring(h.start(), h.end()).replace(" ", "")
                   : hMax != null ? " le matin" : hMin != null ? " l'après-midi" : "");
        if (trajets.isEmpty()) {
            return new AssistantResponse("Je n'ai trouvé aucun trajet" + criteres + " avec des places libres. "
                    + "Essayez un autre jour, ou sans préciser l'heure.", List.of(),
                    List.of("Tous les trajets vers l'ISET", "Un trajet demain", conducteur ? "Comment proposer un trajet ?" : "Mes réservations"));
        }
        String debut = trajets.size() == 1 ? "J'ai trouvé 1 trajet" : "J'ai trouvé " + trajets.size() + " trajets";
        return new AssistantResponse(debut + criteres + ". Ouvrez-en un pour réserver :", trajets,
                List.of("Comment se passe la réservation ?", "Comment payer ?", "Mes réservations"));
    }

    // ---------- Données de l'utilisateur ----------

    private AssistantResponse mesReservations(Utilisateur u, boolean conducteur) {
        List<ReservationResponse> actives = reservationService.reservationsDuPassager(u.getId()).stream()
                .filter(r -> r.statut() == StatutReservation.EN_ATTENTE || r.statut() == StatutReservation.CONFIRMEE)
                .filter(r -> !r.trajet().dateDepart().isBefore(LocalDate.now()))
                .toList();
        if (actives.isEmpty()) {
            return reponse("Vous n'avez aucune réservation à venir. Voulez-vous que je cherche un trajet ?",
                    List.of("Un trajet vers l'ISET demain", "Tous les trajets vers l'ISET", "Comment réserver ?"));
        }
        StringBuilder sb = new StringBuilder("Vos réservations à venir :");
        for (ReservationResponse r : actives) {
            sb.append("\n• ").append(r.trajet().villeDepart()).append(" → ").append(r.trajet().villeArrivee())
                    .append(", ").append(r.trajet().dateDepart().format(COURT)).append(" à ")
                    .append(r.trajet().heureDepart().toString(), 0, 5)
                    .append(r.statut() == StatutReservation.EN_ATTENTE ? " (en attente du conducteur)" : " (confirmée)");
        }
        return new AssistantResponse(sb.toString(), actives.stream().map(ReservationResponse::trajet).toList(),
                List.of("Comment annuler ?", "Comment contacter le conducteur ?", conducteur ? "Mes demandes en attente" : "Un trajet demain"));
    }

    private AssistantResponse mesDemandes(Utilisateur u, boolean conducteur) {
        List<ReservationResponse> demandes = decisionService.demandesEnAttente(u.getId());
        if (demandes.isEmpty()) {
            return reponse("Aucune demande en attente sur vos trajets.", suggestions(conducteur));
        }
        StringBuilder sb = new StringBuilder("Vous avez " + demandes.size() + " demande(s) en attente :");
        for (ReservationResponse r : demandes) {
            sb.append("\n• ").append(r.passagerNom()).append(", ").append(r.nombrePlaces()).append(" place(s) pour ")
                    .append(r.trajet().villeDepart()).append(" → ").append(r.trajet().villeArrivee())
                    .append(" le ").append(r.trajet().dateDepart().format(COURT));
        }
        sb.append("\nRépondez-y dans Mes trajets, onglet Demandes.");
        return reponse(sb.toString(), List.of("Comment accepter une demande ?", "Mes réservations"));
    }

    // ---------- Questions fréquentes ----------

    private AssistantResponse questionFrequente(String t, boolean conducteur) {
        if (contient(t, "annul")) {
            return reponse("Pour annuler : ouvrez Mes trajets, puis cliquez sur Annuler à côté de la réservation. "
                    + "C'est possible tant que le trajet n'est pas parti, et la place est rendue au conducteur.", suggestions(conducteur));
        }
        if (contient(t, "accept", "refus", "attente", "confirm", "expir", "se passe la reservation")) {
            return reponse("Quand vous réservez, votre demande est envoyée au conducteur et la place est bloquée pour vous. "
                    + "Il l'accepte ou la refuse ; vous recevez une notification dans les deux cas. "
                    + "Sans réponse sous 12 h (ou 2 h avant le départ), la demande expire et la place est libérée. "
                    + (conducteur ? "En tant que conducteur, vos demandes sont dans Mes trajets, onglet Demandes." : ""), suggestions(conducteur));
        }
        if (contient(t, "propos", "publi", "devenir conducteur", "vehicule", "ma voiture", "matricule")) {
            return reponse(conducteur
                    ? "Cliquez sur « Proposer un trajet » en haut de la page : choisissez le départ, l'arrivée, la date, "
                      + "le nombre de places et le prix. Vous pouvez aussi placer le point de rendez-vous sur la carte."
                    : "Ouvrez Profil, puis « Activer le mode conducteur » : renseignez le modèle, la couleur et la matricule "
                      + "de votre voiture. Le bouton « Proposer un trajet » sera ensuite disponible.", suggestions(conducteur));
        }
        if (contient(t, "noter", "note", "avis", "etoile")) {
            return reponse("Après un trajet confirmé, un bouton « Noter » apparaît dans Mes trajets. Donnez de 1 à 5 étoiles "
                    + "et un commentaire : la moyenne s'affiche sur le profil du conducteur.", suggestions(conducteur));
        }
        if (contient(t, "pay", "prix", "paie", "argent", "combien", "dinar", "dt")) {
            return reponse("Le prix est fixé par le conducteur, par place. Le paiement se fait directement au conducteur "
                    + "le jour du trajet : l'application ne prend aucun paiement en ligne.", suggestions(conducteur));
        }
        if (contient(t, "contact", "message", "ecrire", "telephone", "numero", "joindre")) {
            return reponse("Sur la page d'un trajet, cliquez sur « Écrire au conducteur ». Vos conversations sont dans Messages. "
                    + "Pour protéger la vie privée, les numéros de téléphone ne sont pas affichés.", suggestions(conducteur));
        }
        if (contient(t, "notif", "rappel", "cloche", "alerte")) {
            return reponse("La cloche en haut de la page affiche vos notifications : nouvelles demandes, réponses du conducteur, "
                    + "messages, et un rappel moins de 24 h avant votre départ.", suggestions(conducteur));
        }
        if (contient(t, "reserv")) {
            return reponse("Cherchez un trajet, ouvrez-le, choisissez le nombre de places puis cliquez sur « Demander à réserver ». "
                    + "Le conducteur confirme ensuite votre place.", suggestions(conducteur));
        }
        if (contient(t, "aide", "help", "que peux", "quoi faire", "fonctionn")) {
            return reponse("Je peux chercher un trajet (« un trajet de Ben Arous vers l'ISET lundi à 8h »), afficher vos "
                    + "réservations, et expliquer comment réserver, annuler, payer, proposer un trajet ou noter un conducteur.",
                    suggestions(conducteur));
        }
        return null;
    }

    // ---------- Outils d'analyse du texte ----------

    private record Ville(String nom, int debut, boolean iset) { }

    private List<Ville> trouverVilles(String texte) {
        Map<String, String> candidats = new LinkedHashMap<>(); // forme normalisée -> nom affiché
        List<String> toutes = new ArrayList<>(VILLES_CONNUES);
        toutes.addAll(trajetRepository.villesDepart());
        toutes.addAll(trajetRepository.villesArrivee());
        for (String v : toutes) {
            if (v != null && !v.isBlank()) candidats.putIfAbsent(normaliser(v), v.trim());
        }
        for (String alias : ALIAS_ISET) candidats.put(alias, "ISET Zaghouan");

        List<String> formes = new ArrayList<>(candidats.keySet());
        formes.sort((a, b) -> b.length() - a.length()); // les plus longues d'abord : « iset zaghouan » avant « zaghouan »
        StringBuilder masque = new StringBuilder(texte);
        List<Ville> resultat = new ArrayList<>();
        for (String forme : formes) {
            Matcher m = Pattern.compile("\\b" + Pattern.quote(forme) + "\\b").matcher(masque);
            while (m.find()) {
                String nom = candidats.get(forme);
                boolean iset = nom.equalsIgnoreCase("ISET Zaghouan");
                resultat.add(new Ville(iset ? "Zaghouan" : nom, m.start(), iset));
                for (int i = m.start(); i < m.end(); i++) masque.setCharAt(i, '#');
            }
        }
        resultat.sort(Comparator.comparingInt(Ville::debut));
        return resultat;
    }

    private LocalDate trouverDate(String t) {
        LocalDate auj = LocalDate.now();
        if (contient(t, "apres demain")) return auj.plusDays(2);
        if (contient(t, "demain")) return auj.plusDays(1);
        if (contient(t, "aujourd hui", "ce soir", "ce matin", "cet apres midi")) return auj;
        String[] jours = {"lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"};
        for (int i = 0; i < jours.length; i++) {
            if (contient(t, jours[i])) {
                DayOfWeek cible = DayOfWeek.of(i + 1);
                LocalDate d = auj;
                while (d.getDayOfWeek() != cible) d = d.plusDays(1);
                return d;
            }
        }
        Matcher m = Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})\\b").matcher(t);
        if (m.find()) {
            try {
                LocalDate d = LocalDate.of(auj.getYear(), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)));
                return d.isBefore(auj) ? d.plusYears(1) : d;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String decrireDate(LocalDate d) {
        if (d.equals(LocalDate.now())) return "aujourd'hui";
        if (d.equals(LocalDate.now().plusDays(1))) return "demain";
        return "le " + d.format(JOUR);
    }

    private static String normaliser(String s) {
        String sansAccents = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.toLowerCase(Locale.ROOT).replaceAll("[’'`]", " ").replaceAll("[^a-z0-9/:\\- ]", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private static boolean contient(String texte, String... mots) {
        for (String mot : mots) {
            if (texte.contains(mot)) return true;
        }
        return false;
    }

    private static AssistantResponse reponse(String texte, List<String> suggestions) {
        return new AssistantResponse(texte, List.of(), suggestions);
    }

    private static List<String> suggestions(boolean conducteur) {
        return conducteur
                ? List.of("Mes demandes en attente", "Un trajet vers l'ISET demain", "Mes réservations", "Comment proposer un trajet ?")
                : List.of("Un trajet vers l'ISET demain", "Mes réservations", "Comment annuler ?", "Comment devenir conducteur ?");
    }
}
