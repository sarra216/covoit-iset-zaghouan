"""
Déploiement automatisé de Covoit ISET Zaghouan avec pyinfra.

Récupère la dernière image publiée par le pipeline dans le registre, redémarre le conteneur
de l'application avec Docker Compose, puis attend que GET /health réponde.

Utilisation (depuis le dossier du projet, qui contient docker-compose.yml et .env) :
    pyinfra @local deploy.py

Réglages par variables d'environnement :
    IMAGE           image à déployer   (défaut : localhost:8085/covoit/covoit-iset:latest)
    COVOIT_DOSSIER  dossier du projet  (défaut : dossier courant)
    COVOIT_URL      adresse à tester   (défaut : http://localhost:3000)

Exemple avec le registre simple :
    IMAGE=localhost:5000/covoit-iset:latest pyinfra @local deploy.py
"""
import os

from pyinfra.operations import server

IMAGE = os.environ.get("IMAGE", "localhost:8085/covoit/covoit-iset:latest")
DOSSIER = os.environ.get("COVOIT_DOSSIER", os.getcwd())
URL = os.environ.get("COVOIT_URL", "http://localhost:3000").rstrip("/")

server.shell(
    name="Vérifier que Docker répond",
    commands=["docker info --format 'Docker {{.ServerVersion}}'"],
)

server.shell(
    name=f"Récupérer la dernière image ({IMAGE})",
    commands=[f"docker pull {IMAGE}"],
)

server.shell(
    name="Redémarrer l'application avec la nouvelle image",
    commands=[f"cd '{DOSSIER}' && IMAGE='{IMAGE}' docker compose up -d --no-build application"],
)

server.shell(
    name="Attendre que /health réponde (90 secondes maximum)",
    commands=[
        f"for i in $(seq 1 30); do "
        f"curl -fsS {URL}/health && echo && exit 0; "
        f"sleep 3; done; "
        f"echo 'L application ne répond pas'; docker compose -f '{DOSSIER}/docker-compose.yml' logs --tail 50 application; exit 1"
    ],
)

server.shell(
    name="Supprimer les anciennes images inutilisées",
    commands=["docker image prune -f"],
)
