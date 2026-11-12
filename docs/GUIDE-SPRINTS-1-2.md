# Déroulement des sprints 1 et 2 sur GitHub

Ce guide explique comment mettre le code dans le dépôt **story par story**, comme en Scrum :
une **branche**, un **commit** et une **pull request** par user story, et la carte qui avance sur le tableau.

---

## 0. Préparation (une seule fois)

### 0.1 Ajouter les statuts au tableau
Dans GitHub Projects : **⋯** → **Settings** → **Status**. Il faut ces options, dans cet ordre :

`Todo` → `Sprint` → `In Progress` → `Done`

### 0.2 Vérifier les workflows automatiques
**⋯** → **Workflows** : activer **Item closed → Status: Done** et **Pull request merged → Status: Done**.
Ainsi, quand une PR contenant `Closes #1` est fusionnée, l'issue #1 se ferme et sa carte passe en **Done** toute seule.

### 0.3 Cloner le dépôt
```bash
git clone https://github.com/sarra216/covoit-iset-zaghouan.git
cd covoit-iset-zaghouan
```
Le dossier `docs/` avec `sprint1.md`, `sprint2.md` et `sprint3.md` est déjà dedans.

### 0.4 Définition de « Terminé » (Definition of Done)
Une story est **Done** quand :
- le code est sur `main` (PR fusionnée) ;
- l'endpoint marche dans Postman (cas normal + cas d'erreur) ;
- la page HTML fonctionne dans le navigateur ;
- l'issue est fermée.

---

## 1. Le cycle d'une user story (à répéter 8 fois)

| Étape | Où | Action |
|---|---|---|
| 1 | Tableau | Carte : **Sprint → In Progress**, et s'assigner dans **Assignees** |
| 2 | Terminal | Créer la branche depuis `main` à jour |
| 3 | Dossier | Copier les fichiers de la story (listes ci-dessous) |
| 4 | Terminal | `git add`, `git commit`, `git push` |
| 5 | GitHub | Créer la pull request avec `Closes #N` dans la description |
| 6 | GitHub | Un coéquipier relit (**Review**) puis **Merge pull request** |
| 7 | Tableau | La carte passe en **Done** automatiquement (sinon, la déplacer) |

Commandes pour l'étape 2 et 4 (exemple avec la story #1) :
```bash
git checkout main
git pull
git checkout -b feature/1-inscription-connexion

# ... copier les fichiers de la story ...

git add .
git commit -m "feat(#1): inscription et connexion des utilisateurs"
git push -u origin feature/1-inscription-connexion
```

Pull request (étape 5) : sur GitHub, cliquer sur **Compare & pull request**.
- **Titre :** `#1 S'inscrire / Se connecter`
- **Description :**
  ```
  Closes #1

  - POST /api/auth/inscription et POST /api/auth/connexion
  - Mot de passe chiffré avec BCrypt
  - Page index.html (onglets connexion / inscription)
  Testé dans Postman : inscription, connexion, mauvais mot de passe (401), email déjà utilisé (409).
  ```

---

## 2. Sprint 1 · 23/10 → 05/11 · Objectif : s'inscrire, proposer un trajet et trouver un trajet

Ordre de développement (selon les dépendances) : **#1 → #26 → #3 → #2**

> Les chemins Java commencent par `src/main/java/tn/iset/covoit/`, les pages par `src/main/resources/static/`.

### #1 S'inscrire / Se connecter · branche `feature/1-inscription-connexion`
- `pom.xml`, `.gitignore`, `README.md`
- `src/main/resources/application.properties`
- `CovoitApplication.java`
- `config/PasswordConfig.java`, `config/WebConfig.java`
- `exception/ApiException.java`, `exception/GlobalExceptionHandler.java`
- `entity/Role.java`, `entity/Utilisateur.java`
- `repository/UtilisateurRepository.java`
- `dto/InscriptionRequest.java`, `dto/ConnexionRequest.java`, `dto/UtilisateurResponse.java`
- `service/AuthService.java`, `controller/AuthController.java`
- `static/index.html`, `static/css/style.css`, `static/js/api.js`

Commit : `feat(#1): inscription et connexion des utilisateurs`

### #26 Proposer un trajet · branche `feature/26-proposer-trajet`
- `entity/Trajet.java`
- `repository/TrajetRepository.java`
- `dto/TrajetRequest.java`, `dto/TrajetResponse.java`
- `service/TrajetService.java`, `controller/TrajetController.java`
- `static/proposer.html`

Commit : `feat(#26): un conducteur peut proposer un trajet`

### #3 Chercher un trajet · branche `feature/3-chercher-trajet`
- `static/recherche.html`
- l'API de recherche (`GET /api/trajets/recherche`) est dans `TrajetService` / `TrajetController`, ajoutés avec #26

Commit : `feat(#3): recherche de trajets par départ, arrivée et date`

### #2 Consulter le profil du conducteur · branche `feature/2-profil-conducteur`
- `dto/ProfilConducteurResponse.java`
- `service/ConducteurService.java`, `controller/ConducteurController.java`
- `static/profil-conducteur.html`

Commit : `feat(#2): profil public du conducteur`

### Fin du sprint 1
1. **Sprint Review :** montrer la démo au professeur (voir `docs/sprint1-bilan.md`).
2. **Rétrospective :** remplir le tableau 👍 / 👎 / 🎯 dans `docs/sprint1-bilan.md`, puis le commiter :
   ```bash
   git checkout main && git pull
   git add docs/sprint1-bilan.md
   git commit -m "docs: bilan du sprint 1 (review et rétrospective)"
   git push
   ```
3. **Marquer la version :** sur GitHub, **Releases** → **Create a new release** → tag `v0.1.0`, titre `Sprint 1`.
4. **Tableau :** les 4 cartes du sprint 1 sont en **Done** ; passer les 4 cartes du sprint 2 en **Sprint**.

---

## 3. Sprint 2 · 06/11 → 19/11 · Objectif : réserver, annuler et contacter le conducteur

Ordre de développement : **#4 → #6 → #11 → #7**
(#6 et #11 ont besoin des réservations créées par #4.)

### #4 Réserver une place · branche `feature/4-reserver-place`
- `entity/StatutReservation.java`, `entity/Reservation.java`
- `repository/ReservationRepository.java`
- `dto/ReservationRequest.java`, `dto/ReservationResponse.java`
- `service/ReservationService.java`, `controller/ReservationController.java`
- le bouton **Réserver** de `recherche.html` (déjà présent depuis #3) devient fonctionnel

Commit : `feat(#4): réservation d'une place avec contrôle des places disponibles`

### #6 Annuler une réservation · branche `feature/6-annuler-reservation`
- `service/AnnulationService.java`, `controller/AnnulationController.java`
- `static/reservations.html`

Commit : `feat(#6): annulation d'une réservation et libération des places`

### #11 Consulter le profil du passager · branche `feature/11-profil-passager`
- `dto/ProfilPassagerResponse.java`
- `service/PassagerService.java`, `controller/PassagerController.java`
- `static/profil-passager.html`

Commit : `feat(#11): profil public du passager`

### #7 Contacter le conducteur · branche `feature/7-contacter-conducteur`
- `entity/Message.java`
- `repository/MessageRepository.java`
- `dto/MessageRequest.java`, `dto/MessageResponse.java`
- `service/MessageService.java`, `controller/MessageController.java`
- `static/messages.html`
- `postman/Covoit-ISET-Zaghouan.postman_collection.json`

Commit : `feat(#7): messagerie entre passager et conducteur`

### Fin du sprint 2 (19/11)
1. **Sprint Review :** démo du scénario complet (voir `docs/sprint2-bilan.md`).
2. **Rétrospective :** remplir et commiter `docs/sprint2-bilan.md`.
3. **Release** `v0.2.0`, titre `Sprint 2`.
4. **Tableau :** les 8 cartes des sprints 1 et 2 sont en **Done** ; passer les 3 cartes du sprint 3 en **Sprint**.

---

## 4. Captures à mettre dans le compte rendu

1. Le tableau en vue **Board** : colonnes Todo / Sprint / In Progress / Done.
2. La liste des **Pull requests** fermées (8 PR, une par story).
3. Une PR ouverte, montrant `Closes #N` et la relecture.
4. **Insights** du projet (graphique d'avancement), filtré sur Sprint 1 puis Sprint 2.
5. Les **Releases** `v0.1.0` et `v0.2.0`.
6. L'application : recherche, réservation, messages.
7. Postman : une requête réussie et une erreur (ex. 409 double réservation).

---

## 5. Si vous n'utilisez pas le terminal

Sur GitHub : changer de branche avec le bouton **main ▾** → taper le nom de la nouvelle branche → **Create branch**,
puis **Add file → Upload files**, glisser les fichiers de la story **dans le bon dossier**, et **Commit changes**.
La pull request se crée ensuite avec le bouton **Compare & pull request**.
