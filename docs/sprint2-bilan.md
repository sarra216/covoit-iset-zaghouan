# Sprint 2 · Bilan

**Dates :** 06/11/2026 → 19/11/2026 (2 semaines)
**Objectif :** réserver, annuler et contacter le conducteur. ✅ **Atteint**

## Sprint Review

| Issue | User story | Complexité | Points | Pull request | Statut |
|---|---|---|---|---|---|
| #4 | Réserver une place | 3 | 8 | #16 | ✅ Fait |
| #6 | Annuler une réservation | 1 | 3 | #17 | ✅ Fait |
| #11 | Consulter le profil du passager | 1 | 2 | #18 | ✅ Fait |
| #7 | Contacter le conducteur | 2 | 5 | #19 | ✅ Fait |
| | **Total** | **7 / 7** | **18** | **4 PR** | **4 / 4** |

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
| Vérification du dépôt de chaque issue avant la PR : toutes les issues se sont fermées automatiquement | Les issues #11, #4, #6, #7 étaient fermées par erreur et ont dû être rouvertes | Ne jamais mettre une carte en « Fait » à la main, laisser la PR le faire |
| Tous les endpoints testés dans Postman (cas normaux et erreurs) | La réservation (complexité 3) a demandé plus de travail que prévu | Découper les grosses stories en tâches |
| Règles métier claires : double réservation, places, annulation | Pas encore de tests automatiques ni de vraie sécurité (JWT) | Ajouter des tests JUnit et prévoir la sécurité dans le backlog |
