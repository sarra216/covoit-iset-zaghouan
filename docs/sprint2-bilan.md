# Sprint 2 · Bilan

**Dates :** 06/11/2026 → 19/11/2026 (2 semaines)
**Objectif :** réserver, annuler et contacter le conducteur. ✅ **Atteint**

## Sprint Review

| Issue | User story | Complexité | Points | Statut |
|---|---|---|---|---|
| #4 | Réserver une place | 3 | 8 | ✅ Done |
| #6 | Annuler une réservation | 1 | 3 | ✅ Done |
| #11 | Consulter le profil du passager | 1 | 2 | ✅ Done |
| #7 | Contacter le conducteur | 2 | 5 | ✅ Done |
| | **Total** | **7 / 7** | **18** | **4 / 4** |

**Vélocité :** 7 de complexité (18 story points), égale au rythme visé. Vélocité moyenne sur 2 sprints : **7**.

**Démonstration :**
1. Le passager réserve 1 place : les places libres passent de 3 à 2.
2. Il essaie de réserver une 2e fois le même trajet : refus « Vous avez déjà réservé ce trajet ».
3. Il contacte le conducteur depuis son profil et le conducteur répond dans **Messages**.
4. Il annule sa réservation : la place est rendue (2 → 3).
5. Son profil passager affiche 0 réservation confirmée et 1 annulée.

**Incrément livré :** API REST (`/api/reservations`, `/api/passagers`, `/api/messages`) + pages `reservations`, `profil-passager`, `messages`, bouton **Réserver** dans `recherche`.

## Sprint Retrospective

| 👍 Ce qui a bien marché | 👎 À améliorer | 🎯 Actions pour le sprint 3 |
|---|---|---|
| Les PR sont relues en moins de 24 h | La réservation (complexité 3) a pris plus de temps que prévu | Découper les grosses stories en tâches |
| Tous les endpoints testés dans Postman | Pas encore de tests automatiques | Ajouter des tests JUnit sur les services |
| Règles métier claires (double réservation, places) | Pas de vraie sécurité (pas de JWT) | Prévoir la sécurité dans le backlog |

> ✏️ À adapter avec ce que votre équipe a vraiment vécu pendant le sprint.
