# 🚗 Covoit ISET Zaghouan

Application de covoiturage pour les étudiants de l'ISET Zaghouan : le **conducteur** propose un trajet,
le **passager** le cherche, le réserve et contacte le conducteur.

**Technologies :** Java 17 · Spring Boot 3 (REST API) · Spring Data JPA · MySQL · HTML / CSS / JavaScript

---

## Fonctionnalités réalisées

| Sprint | Issue | User story | API | Page |
|---|---|---|---|---|
| 1 | #1 | S'inscrire / Se connecter | `POST /api/auth/inscription` · `POST /api/auth/connexion` | `index.html` |
| 1 | #26 | Proposer un trajet | `POST /api/trajets` | `proposer.html` |
| 1 | #3 | Chercher un trajet | `GET /api/trajets/recherche?depart=&arrivee=&date=` | `recherche.html` |
| 1 | #2 | Consulter le profil du conducteur | `GET /api/conducteurs/{id}` | `profil-conducteur.html` |
| 2 | #4 | Réserver une place | `POST /api/reservations` · `GET /api/reservations/passager/{id}` | `recherche.html` (bouton Réserver) |
| 2 | #6 | Annuler une réservation | `PUT /api/reservations/{id}/annulation?passagerId=` | `reservations.html` |
| 2 | #11 | Consulter le profil du passager | `GET /api/passagers/{id}` | `profil-passager.html` |
| 2 | #7 | Contacter le conducteur | `POST /api/messages` · `GET /api/messages/conversation` · `GET /api/messages/utilisateur/{id}` | `messages.html` |

---

## Lancer le projet

1. **Démarrer MySQL** (par exemple avec XAMPP). La base `covoit_iset` est créée automatiquement.
   Si votre utilisateur MySQL a un mot de passe, modifiez `src/main/resources/application.properties` :
   ```properties
   spring.datasource.username=root
   spring.datasource.password=votre_mot_de_passe
   ```
2. **Ouvrir le projet** dans IntelliJ IDEA (ou VS Code) et lancer `CovoitApplication`,
   ou en ligne de commande :
   ```bash
   mvn spring-boot:run
   ```
3. **Ouvrir** http://localhost:8080 dans le navigateur.

Les tables `utilisateurs`, `trajets`, `reservations` et `messages` sont créées automatiquement par JPA.

### Tester avec Postman
Importer `postman/Covoit-ISET-Zaghouan.postman_collection.json` (File → Import), puis lancer les requêtes dans l'ordre.

---

## Scénario de démonstration

1. S'inscrire comme **conducteur** (ex. Ahmed) → proposer un trajet *Tunis → Zaghouan*.
2. Se déconnecter, s'inscrire comme **passager** (ex. Sarra) → chercher *Zaghouan*.
3. Cliquer sur le nom du conducteur → voir son **profil** → **Contacter le conducteur**.
4. Revenir à la recherche → **Réserver** 1 place → les places libres diminuent.
5. **Mes réservations** → **Annuler** → la place est rendue au trajet.
6. **Mon profil** → les compteurs de réservations confirmées / annulées.

---

## Règles métier

- Mot de passe chiffré avec **BCrypt** ; l'email est unique.
- Seul un **conducteur** peut proposer un trajet ; pas de trajet dans le passé ; départ ≠ arrivée.
- La recherche n'affiche que les trajets **à venir** avec des **places libres**.
- On ne peut pas réserver **son propre trajet**, ni **deux fois** le même trajet, ni plus de places que disponibles.
- Deux réservations simultanées de la dernière place sont bloquées (`@Version`, verrou optimiste).
- On ne peut annuler que **sa propre** réservation, **avant** le départ ; les places sont rendues.
- Les profils publics n'affichent **ni email ni téléphone**.

> **Limite connue :** l'utilisateur connecté est gardé dans le navigateur (`localStorage`) et son id est envoyé
> à l'API. Il n'y a pas encore de jeton (JWT) : la sécurisation complète est prévue dans un sprint suivant.

---

## Structure

```
src/main/java/tn/iset/covoit/
├── entity/        Utilisateur, Trajet, Reservation, Message (+ Role, StatutReservation)
├── repository/    accès à la base (Spring Data JPA)
├── dto/           données reçues et renvoyées par l'API
├── service/       règles métier (une classe par user story)
├── controller/    endpoints REST
├── exception/     erreurs renvoyées en JSON { "message": "..." }
└── config/        BCrypt, CORS
src/main/resources/static/   pages HTML, css/style.css, js/api.js
docs/                         objectifs et bilans des sprints
postman/                      collection de tests de l'API
```
