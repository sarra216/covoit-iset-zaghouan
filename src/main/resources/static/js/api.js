// Fonctions communes à toutes les pages

// Si la page est servie par Spring Boot (port 8080) on appelle /api,
// sinon (Live Server, fichier ouvert directement) on appelle http://localhost:8080/api
const API_URL = window.location.port === '8080' ? '/api' : 'http://localhost:8080/api';

/** Appelle l'API et renvoie le JSON, ou lève une erreur avec le message du serveur. */
async function api(chemin, options = {}) {
  const reponse = await fetch(API_URL + chemin, {
    method: options.method || 'GET',
    headers: { 'Content-Type': 'application/json' },
    body: options.body ? JSON.stringify(options.body) : undefined
  });
  const texte = await reponse.text();
  const donnees = texte ? JSON.parse(texte) : null;
  if (!reponse.ok) {
    throw new Error((donnees && donnees.message) || 'Erreur ' + reponse.status);
  }
  return donnees;
}

// ---------- Session (utilisateur enregistré après la connexion) ----------

function utilisateurConnecte() {
  try {
    return JSON.parse(localStorage.getItem('utilisateur'));
  } catch (e) {
    return null;
  }
}

/** Redirige vers la page de connexion si personne n'est connecté. */
function exigerConnexion() {
  const utilisateur = utilisateurConnecte();
  if (!utilisateur) {
    window.location.href = 'index.html';
    throw new Error('Non connecté'); // arrête le script de la page
  }
  return utilisateur;
}

function deconnexion() {
  localStorage.removeItem('utilisateur');
  window.location.href = 'index.html';
}

// ---------- Affichage ----------

/** Protège contre l'injection de HTML dans les textes saisis par les utilisateurs. */
function echapper(texte) {
  return String(texte ?? '').replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

function afficherAlerte(element, texte, type = 'erreur') {
  element.textContent = texte;
  element.className = 'alerte ' + type;
  element.hidden = false;
}

function formaterDate(date) {
  return new Date(date + 'T00:00').toLocaleDateString('fr-FR', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
  });
}

function formaterHeure(heure) {
  return heure ? heure.slice(0, 5) : '';
}

function formaterDateHeure(dateHeure) {
  return new Date(dateHeure).toLocaleString('fr-FR', {
    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
  });
}

/** Barre de navigation commune. "page" = nom de la page active. */
function afficherNav(page) {
  const u = utilisateurConnecte();
  if (!u) return;
  const profil = u.role === 'CONDUCTEUR'
    ? 'profil-conducteur.html?id=' + u.id
    : 'profil-passager.html?id=' + u.id;
  const liens = [
    ['recherche', 'recherche.html', 'Chercher un trajet'],
    ...(u.role === 'CONDUCTEUR' ? [['proposer', 'proposer.html', 'Proposer un trajet']] : []),
    ['reservations', 'reservations.html', 'Mes réservations'],
    ['messages', 'messages.html', 'Messages'],
    ['profil', profil, 'Mon profil']
  ];
  const nav = document.createElement('nav');
  nav.className = 'nav';
  nav.innerHTML =
    '<span class="logo">🚗 Covoit ISET Zaghouan</span>' +
    liens.map(([cle, href, texte]) =>
      `<a href="${href}" class="${cle === page ? 'actif' : ''}">${texte}</a>`).join('') +
    `<button onclick="deconnexion()">Déconnexion (${echapper(u.prenom)})</button>`;
  document.body.prepend(nav);
}

/** Carte HTML d'un trajet ; "actions" = HTML des boutons à droite. */
function carteTrajet(t, actions = '') {
  return `
    <div class="carte trajet">
      <div>
        <div class="itineraire">${echapper(t.villeDepart)} → ${echapper(t.villeArrivee)}</div>
        <div class="details">
          ${formaterDate(t.dateDepart)} à ${formaterHeure(t.heureDepart)}
          · ${t.placesDisponibles}/${t.placesTotales} place(s) libre(s)
          · Conducteur : <a href="profil-conducteur.html?id=${t.conducteurId}">${echapper(t.conducteurNom)}</a>
        </div>
      </div>
      <div class="actions">
        <span class="prix">${t.prix} DT</span>
        ${actions}
      </div>
    </div>`;
}
