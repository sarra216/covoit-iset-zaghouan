#!/bin/bash
# Déploiement sans pyinfra (même résultat que deploy.py), si pyinfra n'est pas installé.
# Utilisation : ./deploy.sh   (ou IMAGE=localhost:5000/covoit-iset:latest ./deploy.sh)
set -e
IMAGE=${IMAGE:-localhost:8085/covoit/covoit-iset:latest}
URL=${COVOIT_URL:-http://localhost:3000}

echo "Récupération de $IMAGE..."
docker pull "$IMAGE"
echo "Redémarrage de l'application..."
IMAGE="$IMAGE" docker compose up -d --no-build application

echo "Attente de $URL/health..."
for i in $(seq 1 30); do
  if curl -fsS "$URL/health"; then echo; echo "Déploiement réussi."; exit 0; fi
  sleep 3
done
echo "L'application ne répond pas :"
docker compose logs --tail 50 application
exit 1
