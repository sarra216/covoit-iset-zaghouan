# Covoit ISET Zaghouan

Application web de covoiturage pour les étudiants de l'ISET Zaghouan : un **conducteur** propose un trajet,
un **passager** le trouve et demande une place, le conducteur **accepte ou refuse**, puis ils échangent
par messagerie et le passager **note** le conducteur après le trajet.

Projet réalisé en Scrum par **Khadija Benbrahim** et **Chayma Tahri**, encadré par **Mme Chifa Labidi**.

**Technologies :** Java 17 · Spring Boot 3 · Spring Security (JWT) · Spring Data JPA · MySQL · HTML / CSS / JavaScript · Leaflet (OpenStreetMap)
**DevOps :** Git · GitLab CE · GitLab CI/CD · GitLab Runner · Docker · Docker Compose · Harbor · pyinfra · Uptime Kuma

---

## Fonctionnalités

| Domaine | Ce que l'application permet |
|---|---|
| Comptes | Inscription avec acceptation des conditions, connexion par jeton JWT, mot de passe oublié, vérification de l'email, suppression du compte |
| Trajets | Proposer un trajet (point de rendez-vous sur la carte), rechercher, trajets populaires, annulation par le conducteur |
| Réservations | Demande de place, acceptation ou refus par le conducteur, expiration automatique (12 h ou 2 h avant le départ), annulation |
| Échanges | Messagerie, notifications (cloche), rappel avant le départ, notes et avis sur le conducteur |
| Assistant | Chatbot en français : « un trajet de Tunis vers l'ISET demain matin », questions fréquentes |
| Sécurité | Mots de passe BCrypt, 5 essais de connexion maximum, signaler ou bloquer un utilisateur |
| Administration | Statistiques, traitement des signalements, suspension des comptes |
| Supervision | `GET /health` : état de l'application, de la base et version déployée |

---

## Architecture DevOps

| Outil | Rôle |
|---|---|
| **GitLab CE** (conteneur local, port 8929) | Gestion du code source, Merge Requests, CI/CD |
| **GitLab Runner** (Shell executor, tag `local`) | Exécute les jobs du pipeline sur la machine |
| **Docker / Docker Compose** | Image de l'application (multi-stage, utilisateur non-root) + base MySQL |
| **Harbor** (conteneur local, port 8085) | Registre Docker privé : reçoit les images construites par le CI |
| **pyinfra** (`deploy.py`) | Déploiement automatisé : récupère la nouvelle image et redémarre le conteneur |
| **Uptime Kuma** (port 3001) | Supervision : interroge `/health` toutes les 60 secondes |

```
 Développeur ──push──▶ GitLab ──jobs──▶ Runner ──image──▶ Harbor
                         │                                  │
                  Merge Request                      docker pull
                    (tests CI)                            │
                                                 pyinfra (deploy.py)
                                                          │
 Uptime Kuma ──GET /health toutes les 60 s──▶ Application :3000 ──▶ MySQL
```

## Flux du pipeline

1. Le développeur pousse sur une branche `feature/...` (personne ne peut pousser directement sur `main`).
2. La Merge Request exécute le job **test** : `mvn -B test` (tests unitaires JUnit 5 / Mockito).
3. La fusion dans `main` exécute **build** (image `covoit-iset:<commit>`) puis **push** (envoi vers Harbor).
4. `pyinfra @local deploy.py` récupère la nouvelle image et redémarre le conteneur, puis vérifie `/health`.
5. Uptime Kuma sonde `/health` en continu : vert si l'application et la base répondent, rouge sinon.

---

## Comment exécuter

### En développement (Eclipse + XAMPP)

1. Démarrer MySQL (XAMPP) et créer la base `covoit_iset`.
2. Lancer `CovoitApplication` (argument `--server.port=8082`).
3. Ouvrir http://localhost:8082.

### Avec Docker Compose

```bash
cp .env.exemple .env              # puis remplir les mots de passe et JWT_SECRET
docker compose up -d --build      # application + MySQL
curl http://localhost:3000/health # → {"status":"ok","version":"dev","base":"ok"}
docker compose down
```

### Pipeline et déploiement

```bash
# Envoyer le code sur GitLab (via une branche et une Merge Request)
git checkout -b feature/ma-fonctionnalite
git push gitlab feature/ma-fonctionnalite

# Après la fusion dans main et le pipeline réussi : déployer la nouvelle image
pyinfra @local deploy.py          # ou ./deploy.sh
curl http://localhost:3000/health

# Démonstration complète en 30 secondes
./demo.sh
```

### Supervision

```bash
docker run -d --name uptime-kuma --restart unless-stopped \
  -p 3001:3001 -v uptime-kuma-data:/app/data louislam/uptime-kuma:1
# http://localhost:3001 › moniteur HTTP(s) › http://host.docker.internal:3000/health
```

### Tests

| Commande | Tests |
|---|---|
| `mvn test` | Tests unitaires (services, sécurité JWT, santé) : sans base de données, aussi lancés par le CI |
| `mvn test -Pe2e` | Tests Selenium de bout en bout : l'application doit tourner et Chrome être installé |

---

## Structure du projet

```
src/main/java/tn/iset/covoit/
  controller/   API REST (+ HealthController : GET /health)
  service/      règles métier
  repository/   accès aux données (Spring Data JPA)
  entity/       tables de la base
  dto/          données échangées avec le navigateur
  security/     JWT, filtre et règles d'accès
src/main/resources/static/   pages HTML, CSS, JavaScript
src/test/java/               tests unitaires et tests Selenium (e2e/)
Dockerfile · docker-compose.yml · .gitlab-ci.yml · deploy.py · deploy.sh · demo.sh
```
