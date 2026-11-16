#!/bin/bash
# Démonstration de la boucle DevOps complète en 30 secondes :
# modification du code → branche → push → Merge Request → (fusion) → pipeline → déploiement.
#
# Utilisation : ./demo.sh
# Réglages : GITLAB_URL (défaut http://localhost:8929), GITLAB_PROJET (défaut root/covoit-iset-zaghouan),
#            REMOTE (nom du dépôt distant GitLab, défaut gitlab)
set -e

GITLAB_URL=${GITLAB_URL:-http://localhost:8929}
GITLAB_PROJET=${GITLAB_PROJET:-root/covoit-iset-zaghouan}
REMOTE=${REMOTE:-gitlab}
FICHIER=src/main/java/tn/iset/covoit/controller/HealthController.java

# On alterne entre "ok" et "healthy" pour pouvoir rejouer la démo plusieurs fois
if grep -q 'STATUT_OK = "healthy"' "$FICHIER"; then ANCIEN=healthy; NOUVEAU=ok; else ANCIEN=ok; NOUVEAU=healthy; fi

echo "1. Modification du code : /health répondra \"$NOUVEAU\" au lieu de \"$ANCIEN\"..."
git checkout main
git pull "$REMOTE" main
BRANCHE="demo-$(date +%s)"
git checkout -b "$BRANCHE"
sed -i "s/STATUT_OK = \"$ANCIEN\"/STATUT_OK = \"$NOUVEAU\"/" "$FICHIER"
git diff --stat

echo "2. Commit et push..."
git add "$FICHIER"
git commit -m "demo: /health répond $NOUVEAU"
git push "$REMOTE" HEAD

echo "3. Ouvrir une Merge Request à l'adresse :"
echo "   $GITLAB_URL/$GITLAB_PROJET/-/merge_requests/new?merge_request%5Bsource_branch%5D=$BRANCHE"
echo
echo "4. Après la fusion et le pipeline (test → build → push), déployer :"
echo "   pyinfra @local deploy.py"
echo "5. Vérifier :"
echo "   curl http://localhost:3000/health   → \"status\":\"$NOUVEAU\" et la nouvelle version"
