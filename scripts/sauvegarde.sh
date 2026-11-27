#!/usr/bin/env bash
# Sauvegarde quotidienne de la base MySQL lancée avec docker compose.
# Usage : ./scripts/sauvegarde.sh            (à lancer depuis le dossier du projet)
# Automatique chaque nuit à 3 h : crontab -e, puis ajouter la ligne
#   0 3 * * * cd /chemin/vers/covoit-iset-zaghouan && ./scripts/sauvegarde.sh >> sauvegardes/journal.txt 2>&1
set -euo pipefail

DOSSIER="sauvegardes"
GARDER_JOURS=14
mkdir -p "$DOSSIER"
FICHIER="$DOSSIER/covoit-$(date +%Y-%m-%d_%H%M).sql.gz"

# Le mot de passe root est lu dans .env
source .env

docker compose exec -T base \
  mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines covoit \
  | gzip > "$FICHIER"

echo "$(date '+%F %T') Sauvegarde créée : $FICHIER ($(du -h "$FICHIER" | cut -f1))"

# On ne garde que les 14 derniers jours
find "$DOSSIER" -name 'covoit-*.sql.gz' -mtime +"$GARDER_JOURS" -delete

# Restaurer une sauvegarde :
#   gunzip -c sauvegardes/covoit-AAAA-MM-JJ_HHMM.sql.gz | docker compose exec -T base mysql -u root -p"$MYSQL_ROOT_PASSWORD" covoit
