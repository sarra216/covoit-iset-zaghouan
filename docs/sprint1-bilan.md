# Sprint 1 · Bilan

**Dates :** 23/10/2026 → 05/11/2026 (2 semaines)
**Objectif :** s'inscrire, proposer un trajet et trouver un trajet. ✅ **Atteint**

## Sprint Review

| Issue | User story | Complexité | Points | Pull request | Statut |
|---|---|---|---|---|---|
| #1 | S'inscrire / Se connecter | 2 | 5 | #12 | ✅ Fait |
| #26 | Proposer un trajet | 2 | 5 | #13 | ✅ Fait |
| #3 | Chercher un trajet | 2 | 5 | #14 | ✅ Fait |
| #2 | Consulter le profil du conducteur | 1 | 2 | #15 | ✅ Fait |
| | **Total** | **7 / 7** | **17** | **4 PR** | **4 / 4** |

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
| Une branche et une pull request par user story | L'issue #26 venait du dépôt biblio-etudiants : « Closes #26 » ne l'a pas fermée | Vérifier le dépôt de chaque issue avant d'ouvrir la PR |
| Le tableau se met à jour tout seul quand une PR est fusionnée | Des issues des sprints 2 et 3 étaient marquées « Fait » sans être réalisées | Rouvrir ces issues et ne fermer une issue que par sa PR |
| Ordre logique respecté : #1 → #26 → #3 → #2 | Mise en place de Git Bash longue (copier-coller, chemins) | Préparer l'environnement avant le début du sprint |
