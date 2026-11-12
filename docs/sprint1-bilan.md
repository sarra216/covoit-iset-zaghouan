# Sprint 1 · Bilan

**Dates :** 23/10/2026 → 05/11/2026 (2 semaines)
**Objectif :** s'inscrire, proposer un trajet et trouver un trajet. ✅ **Atteint**

## Sprint Review

| Issue | User story | Complexité | Points | Statut |
|---|---|---|---|---|
| #1 | S'inscrire / Se connecter | 2 | 5 | ✅ Done |
| #26 | Proposer un trajet | 2 | 5 | ✅ Done |
| #3 | Chercher un trajet | 2 | 5 | ✅ Done |
| #2 | Consulter le profil du conducteur | 1 | 2 | ✅ Done |
| | **Total** | **7 / 7** | **17** | **4 / 4** |

**Vélocité :** 7 de complexité (17 story points), égale au rythme visé.

**Démonstration :**
1. Un étudiant crée un compte conducteur, puis se connecte.
2. Il propose le trajet *Tunis → Zaghouan*, demain à 7h30, 3 places, 5 DT.
3. Un passager cherche « Zaghouan » et voit le trajet.
4. Il clique sur le nom du conducteur et consulte son profil.

**Incrément livré :** API REST (`/api/auth`, `/api/trajets`, `/api/conducteurs`) + pages `index`, `proposer`, `recherche`, `profil-conducteur`.

## Sprint Retrospective

| 👍 Ce qui a bien marché | 👎 À améliorer | 🎯 Actions pour le sprint 2 |
|---|---|---|
| Une branche et une pull request par user story | Certaines PR sont restées longtemps sans relecture | Relire une PR dans les 24 h |
| Le tableau GitHub Projects était toujours à jour | Tests faits seulement à la fin du sprint | Tester chaque endpoint dans Postman avant la PR |
| Ordre logique : #1 → #26 → #3 → #2 | Messages d'erreur pas clairs au début | Un message d'erreur clair pour chaque règle métier |

> ✏️ À adapter avec ce que votre équipe a vraiment vécu pendant le sprint.
