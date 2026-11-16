// =========================================================
// Fonctions communes à toutes les pages
// =========================================================

// Si la page est servie par Spring Boot (quel que soit le port) on appelle /api,
// sinon (Live Server sur le port 5500, ou fichier ouvert directement) on appelle http://localhost:8080/api
const API_URL = (window.location.protocol.startsWith('http') && window.location.port !== '5500')
  ? '/api'
  : 'http://localhost:8080/api';

/** Appelle l'API et renvoie le JSON, ou lève une erreur avec le message du serveur. */
async function api(chemin, options = {}) {
  let reponse;
  const entetes = { 'Content-Type': 'application/json' };
  const jeton = jetonSession();
  if (jeton) entetes.Authorization = 'Bearer ' + jeton; // jeton JWT reçu à la connexion
  try {
    reponse = await fetch(API_URL + chemin, {
      method: options.method || 'GET',
      headers: entetes,
      body: options.body ? JSON.stringify(options.body) : undefined
    });
  } catch (e) {
    throw new Error('Serveur injoignable. Vérifiez que l\'application est démarrée.');
  }
  // Jeton absent ou expiré : retour à la page de connexion
  if (reponse.status === 401 && !chemin.startsWith('/auth/')) {
    oublierSession();
    window.location.href = 'index.html?expire=1';
    throw new Error('Session expirée, veuillez vous reconnecter');
  }
  const texte = await reponse.text();
  let donnees = null;
  try { donnees = texte ? JSON.parse(texte) : null; } catch (e) { donnees = null; }
  if (!reponse.ok) {
    throw new Error((donnees && donnees.message) || 'Erreur ' + reponse.status);
  }
  return donnees;
}

// ---------- Session ----------

function jetonSession() {
  try { return localStorage.getItem('jeton'); } catch (e) { return null; }
}

/** L'utilisateur n'est considéré connecté que s'il a aussi un jeton. */
function utilisateurConnecte() {
  try {
    return jetonSession() ? JSON.parse(localStorage.getItem('utilisateur')) : null;
  } catch (e) {
    return null;
  }
}

/** Réponse de /auth/connexion ou /auth/inscription : { token, utilisateur }. */
function enregistrerSession(reponse) {
  try { localStorage.setItem('jeton', reponse.token); } catch (e) { /* navigation privée */ }
  enregistrerUtilisateur(reponse.utilisateur);
}

function oublierSession() {
  try {
    localStorage.removeItem('utilisateur');
    localStorage.removeItem('jeton');
  } catch (e) { /* rien */ }
}

function enregistrerUtilisateur(utilisateur) {
  try { localStorage.setItem('utilisateur', JSON.stringify(utilisateur)); } catch (e) { /* navigation privée */ }
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
  oublierSession();
  window.location.href = 'index.html';
}

function estConducteur(u) {
  return u && u.role === 'CONDUCTEUR';
}

function aVehicule(u) {
  return u && u.voitureModele && u.voitureCouleur && u.matricule;
}

function parametre(nom) {
  return new URLSearchParams(window.location.search).get(nom);
}

// ---------- Icônes (traits inspirés de Lucide, licence ISC) ----------

const ICONES = {
  recherche: '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
  voiture: '<path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/><circle cx="7" cy="17" r="2"/><path d="M9 17h6"/><circle cx="17" cy="17" r="2"/>',
  message: '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>',
  utilisateur: '<path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
  utilisateurs: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  cloche: '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>',
  echanger: '<path d="m21 16-4 4-4-4"/><path d="M17 20V4"/><path d="m3 8 4-4 4 4"/><path d="M7 4v16"/>',
  calendrier: '<rect width="18" height="18" x="3" y="4" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/>',
  horloge: '<circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>',
  epingle: '<path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/>',
  cercle: '<circle cx="12" cy="12" r="8"/>',
  droite: '<path d="m9 18 6-6-6-6"/>',
  gauche: '<path d="m15 18-6-6 6-6"/>',
  fleche: '<path d="M5 12h14"/><path d="m12 5 7 7-7 7"/>',
  plus: '<path d="M5 12h14"/><path d="M12 5v14"/>',
  moins: '<path d="M5 12h14"/>',
  ok: '<path d="M20 6 9 17l-5-5"/>',
  croix: '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
  sortie: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/>',
  envoyer: '<path d="m22 2-7 20-4-9-9-4Z"/><path d="M22 2 11 13"/>',
  viser: '<circle cx="12" cy="12" r="7"/><path d="M12 2v3"/><path d="M12 19v3"/><path d="M2 12h3"/><path d="M19 12h3"/><circle cx="12" cy="12" r="2"/>',
  billet: '<rect width="20" height="12" x="2" y="6" rx="2"/><circle cx="12" cy="12" r="2"/><path d="M6 12h.01M18 12h.01"/>',
  bouclier: '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"/><path d="m9 12 2 2 4-4"/>',
  crayon: '<path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"/>',
  alerte: '<circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/>',
  route: '<circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/>',
  palette: '<circle cx="13.5" cy="6.5" r="1.5"/><circle cx="17.5" cy="10.5" r="1.5"/><circle cx="8.5" cy="7.5" r="1.5"/><circle cx="6.5" cy="12.5" r="1.5"/><path d="M12 2a10 10 0 0 0 0 20c.9 0 1.7-.8 1.7-1.7 0-.4-.2-.8-.4-1.1-.3-.3-.4-.7-.4-1.1 0-.9.8-1.7 1.7-1.7h2A5.6 5.6 0 0 0 22 11 10 10 0 0 0 12 2"/>',
  courriel: '<rect width="20" height="16" x="2" y="4" rx="2"/><path d="m22 7-10 6L2 7"/>',
  menu: '<path d="M4 6h16"/><path d="M4 12h16"/><path d="M4 18h16"/>',
  etoile: '<path d="M12 2.5l2.9 6 6.6.9-4.8 4.6 1.2 6.5L12 17.4l-5.9 3.1 1.2-6.5L2.5 9.4l6.6-.9z"/>',
  assistant: '<path d="M12 8V4H8"/><rect width="16" height="12" x="4" y="8" rx="2"/><path d="M2 14h2"/><path d="M20 14h2"/><path d="M15 13v2"/><path d="M9 13v2"/>',
  telephone: '<path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1 1 .4 1.9.7 2.8a2 2 0 0 1-.5 2.1L8 9.9a16 16 0 0 0 6 6l1.3-1.3a2 2 0 0 1 2.1-.4c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2z"/>'
};

function ic(nom) {
  return `<svg class="ic" viewBox="0 0 24 24" aria-hidden="true">${ICONES[nom] || ''}</svg>`;
}

// ---------- Affichage ----------

/** Protège contre l'injection de HTML dans les textes saisis par les utilisateurs. */
function echapper(texte) {
  return String(texte ?? '').replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

function ville(nom) {
  // « tunis » -> « Tunis », sans toucher à « ISET Zaghouan »
  const v = String(nom || '').trim();
  return v === v.toLowerCase() ? v.replace(/(^|[\s-])\p{L}/gu, c => c.toUpperCase()) : v;
}

function formaterDate(date) {
  const d = new Date(date + 'T00:00');
  const aujourdhui = new Date(); aujourdhui.setHours(0, 0, 0, 0);
  const ecart = Math.round((d - aujourdhui) / 86400000);
  if (ecart === 0) return "aujourd'hui";
  if (ecart === 1) return 'demain';
  return d.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
}

function formaterJourCourt(date) {
  const d = new Date(date + 'T00:00');
  const aujourdhui = new Date(); aujourdhui.setHours(0, 0, 0, 0);
  const ecart = Math.round((d - aujourdhui) / 86400000);
  if (ecart === 0) return "Auj.";
  if (ecart === 1) return 'Demain';
  return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
}

function formaterHeure(heure) {
  return heure ? heure.slice(0, 5) : '';
}

function formaterPrix(prix) {
  const n = Number(prix);
  return (n % 1 === 0 ? n : n.toFixed(1).replace('.', ',')) + ' DT';
}

function formaterMoment(dateHeure) {
  const d = new Date(dateHeure);
  if (d.toDateString() === new Date().toDateString()) {
    return d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }
  return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
}

function dateDuJour() {
  const d = new Date();
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
}

function estPasse(t) {
  return new Date(t.dateDepart + 'T' + t.heureDepart) < new Date();
}

/** Avatar rond avec les initiales et une couleur stable par personne. */
function avatar(nom, taille = '') {
  const morceaux = String(nom || '?').trim().split(/\s+/);
  const initiales = ((morceaux[0] || '?')[0] + (morceaux.length > 1 ? morceaux[morceaux.length - 1][0] : '')).toUpperCase();
  const couleurs = ['#1e4b8f', '#2f6f5e', '#7a4a9e', '#b4542e', '#3b6ea8', '#6a5a1f', '#8a3b5c', '#2d5f7a'];
  let h = 0;
  for (const c of String(nom)) h = (h * 31 + c.charCodeAt(0)) >>> 0;
  return `<span class="avatar ${taille}" style="background:${couleurs[h % couleurs.length]}" aria-hidden="true">${echapper(initiales)}</span>`;
}

function etatVide(icone, titre, texte, action = '') {
  return `<div class="vide"><span class="rond">${ic(icone)}</span><strong>${titre}</strong><div>${texte}</div>${action}</div>`;
}

function chargement(n = 3) {
  return '<div class="chargement"></div>'.repeat(n);
}

// ---------- Messages éphémères ----------

function toast(texte, type = 'succes') {
  let zone = document.querySelector('.toasts');
  if (!zone) {
    zone = document.createElement('div');
    zone.className = 'toasts';
    zone.setAttribute('role', 'status');
    document.body.appendChild(zone);
  }
  const el = document.createElement('div');
  el.className = 'toast ' + (type === 'erreur' ? 'erreur' : '');
  el.innerHTML = ic(type === 'erreur' ? 'alerte' : 'ok') + `<span>${echapper(texte)}</span>`;
  zone.appendChild(el);
  setTimeout(() => el.remove(), 4000);
}

// ---------- Fenêtre modale ----------

function ouvrirFenetre(html) {
  const voile = document.createElement('div');
  voile.className = 'voile';
  voile.innerHTML = `<div class="fenetre" role="dialog" aria-modal="true">${html}</div>`;
  const fermer = () => { voile.remove(); document.removeEventListener('keydown', echap); };
  const echap = e => { if (e.key === 'Escape') fermer(); };
  voile.addEventListener('click', e => { if (e.target === voile) fermer(); });
  document.addEventListener('keydown', echap);
  document.body.appendChild(voile);
  voile.querySelector('button, a, input')?.focus();
  return { element: voile.querySelector('.fenetre'), fermer };
}

/** Demande une confirmation ; renvoie une promesse true / false. */
function confirmer(titre, texte, libelle = 'Confirmer', danger = false) {
  return new Promise(resolve => {
    const { element, fermer } = ouvrirFenetre(`
      <h3>${titre}</h3>
      <p class="chapeau">${texte}</p>
      <div class="rangee" style="justify-content:flex-end">
        <button class="btn contour" data-r="non">Retour</button>
        <button class="btn ${danger ? 'danger' : ''}" data-r="oui">${libelle}</button>
      </div>`);
    element.querySelectorAll('[data-r]').forEach(b => b.addEventListener('click', () => {
      fermer();
      resolve(b.dataset.r === 'oui');
    }));
  });
}

// ---------- Navigation : barre du haut (ordinateur) + onglets du bas (téléphone) ----------

function afficherNav(page) {
  const u = utilisateurConnecte();
  if (!u) return;
  const liens = [
    ['recherche', 'recherche.html', 'recherche', 'Rechercher'],
    ['trajets', 'reservations.html', 'voiture', 'Mes trajets'],
    ['messages', 'messages.html', 'message', 'Messages'],
    ['profil', 'profil.html', 'utilisateur', 'Profil']
  ];
  const nom = `${u.prenom} ${u.nom}`;
  const haut = document.createElement('header');
  haut.className = 'barre-haut';
  haut.innerHTML = `
    <div class="conteneur">
      <a class="logo" href="recherche.html"><span class="plaque-logo">${ic('voiture')}</span>Covoit ISET</a>
      <nav class="nav-haut" aria-label="Navigation principale">
        ${liens.slice(0, 3).map(([cle, href, , texte]) =>
          `<a href="${href}" class="${cle === page ? 'actif' : ''}" ${cle === page ? 'aria-current="page"' : ''}>${texte}</a>`).join('')}
        ${u.role === 'ADMIN' ? `<a href="admin.html" class="${page === 'admin' ? 'actif' : ''}">Administration</a>` : ''}
      </nav>
      <div class="droite">
        <div class="cloche-zone">
          <button class="btn-icone cloche" aria-label="Notifications" aria-expanded="false">${ic('cloche')}<span class="pastille" hidden></span></button>
          <div class="panneau-notifs" hidden></div>
        </div>
        <a class="btn jaune petit btn-proposer" href="proposer.html">${ic('plus')} Proposer un trajet</a>
        <a class="compte" href="profil.html" aria-label="Mon profil">${avatar(nom, 's')}<span class="nom-compte">${echapper(u.prenom)}</span></a>
      </div>
    </div>`;
  document.body.prepend(haut);

  const bas = document.createElement('nav');
  bas.className = 'onglets-bas';
  bas.setAttribute('aria-label', 'Navigation');
  bas.innerHTML = liens.map(([cle, href, icone, texte]) =>
    `<a href="${href}" class="${cle === page ? 'actif' : ''}">${ic(icone)}${texte}</a>`).join('');
  document.body.appendChild(bas);

  // « Proposer un trajet » : il faut être conducteur avec un véhicule enregistré
  haut.querySelector('.btn-proposer').addEventListener('click', e => {
    if (!estConducteur(u) || !aVehicule(u)) {
      e.preventDefault();
      location.href = 'completer-profil.html?suite=proposer';
    }
  });

  brancherNotifications(haut, u);
  installerAssistant(u);
}

/** Lien « Retour » en haut du contenu. */
function lienRetour(texte = 'Retour', repli = 'recherche.html') {
  return `<button class="retour" onclick="history.length > 1 ? history.back() : location.href='${repli}'">${ic('gauche')}${texte}</button>`;
}

// ---------- Villes proposées dans les champs De / À ----------

const VILLES = ['ISET Zaghouan', 'Zaghouan', 'Tunis', 'Ben Arous', 'Ariana', 'La Manouba', 'El Fahs', 'Zriba',
  'Bir Mcherga', 'Nadhour', 'Saouaf', 'Mornag', 'Mohamedia', 'Hammam Lif', 'Hammam Zriba', 'Nabeul', 'Hammamet',
  'Enfidha', 'Sousse', 'Grombalia', 'Bou Argoub', 'Megrine', 'Rades', 'Fouchana'];

function ajouterListeVilles() {
  if (document.getElementById('villes')) return;
  const liste = document.createElement('datalist');
  liste.id = 'villes';
  liste.innerHTML = VILLES.map(v => `<option value="${v}">`).join('');
  document.body.appendChild(liste);
}

// ---------- Une ligne de trajet ----------

function etatPlaces(libres) {
  if (libres === 0) return '<span class="places complet">Complet</span>';
  return `<span class="places ${libres === 1 ? 'peu' : ''}">${libres} place${libres > 1 ? 's' : ''} libre${libres > 1 ? 's' : ''}</span>`;
}

function ligneTrajet(t, extra = '', avecJour = false) {
  const voiture = [t.voitureModele, t.voitureCouleur].filter(Boolean).map(echapper).join(', ');
  return `
    <div class="ligne-trajet" data-lien="trajet.html?id=${t.id}">
      <div class="quand-trajet">${formaterHeure(t.heureDepart)}${avecJour ? `<small>${formaterJourCourt(t.dateDepart)}</small>` : ''}</div>
      <div class="route-col">
        <a class="itineraire" href="trajet.html?id=${t.id}">${echapper(ville(t.villeDepart))} ${ic('fleche')} ${echapper(ville(t.villeArrivee))}</a>
        <div class="lieu">${t.lieuDepart ? 'Départ : ' + echapper(t.lieuDepart) : 'Point de départ à convenir'}</div>
      </div>
      <div class="conducteur">${avatar(t.conducteurNom, 's')}<div><strong>${echapper(t.conducteurNom)}${t.nombreAvis ? ` <span class="note-mini">${ic('etoile')}${noteTexte(t.noteConducteur)}</span>` : ''}</strong><span>${voiture || 'Conducteur'}</span></div></div>
      <div class="tarif"><strong>${formaterPrix(t.prix)}</strong>${etatPlaces(t.placesDisponibles)}</div>
      ${extra ? `<div class="extra">${extra}</div>` : ''}
    </div>`;
}

// Toute la ligne est cliquable ; les liens et boutons à l'intérieur gardent leur propre action
document.addEventListener('click', e => {
  const ligne = e.target.closest('.ligne-trajet[data-lien]');
  if (!ligne || e.target.closest('a, button, input')) return;
  location.href = ligne.dataset.lien;
});

// ---------- Carte géographique (Leaflet + OpenStreetMap) ----------

const CENTRE_ZAGHOUAN = [36.4029, 10.1429];

function carteDisponible() {
  return typeof L !== 'undefined';
}

function creerCarte(idElement, centre, zoom = 15, options = {}) {
  const carte = L.map(idElement, { attributionControl: true, ...options }).setView(centre, zoom);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19, attribution: '© OpenStreetMap'
  }).addTo(carte);
  return carte;
}

function marqueur(carte, position) {
  return L.circleMarker(position, { radius: 10, color: '#fff', weight: 3, fillColor: '#1e4b8f', fillOpacity: 1 }).addTo(carte);
}

/** Ouvre le sélecteur de lieu ; renvoie une promesse { lieu, latitude, longitude } ou null. */
function choisirLieuSurCarte(depart) {
  return new Promise(resolve => {
    if (!carteDisponible()) {
      toast('La carte n\'a pas pu être chargée.', 'erreur');
      resolve(null);
      return;
    }
    const conteneur = document.createElement('div');
    conteneur.className = 'selecteur-carte';
    conteneur.innerHTML = `
      <div class="cadre" role="dialog" aria-modal="true" aria-label="Choisir le point de départ">
        <div id="carte-choix"></div>
        <div class="barre-lieu">
          <button class="btn-icone" data-action="fermer" aria-label="Fermer">${ic('croix')}</button>
          <div class="entree">${ic('recherche')}<input id="champ-lieu" placeholder="Rechercher un lieu en Tunisie" autocomplete="off"></div>
          <button class="btn-icone" data-action="position" aria-label="Utiliser ma position">${ic('viser')}</button>
        </div>
        <div class="suggestions" hidden></div>
        <div class="viseur">${ic('epingle')}</div>
        <div class="panneau-bas">
          <div class="texte"><span>Point de départ</span><strong id="nom-lieu">Déplacez la carte…</strong></div>
          <button class="btn" data-action="confirmer">${ic('ok')} Confirmer ce point</button>
        </div>
      </div>`;
    document.body.appendChild(conteneur);

    const centre = depart && depart.latitude ? [depart.latitude, depart.longitude] : CENTRE_ZAGHOUAN;
    const carte = creerCarte('carte-choix', centre, 16, { zoomControl: false });
    const nomLieu = conteneur.querySelector('#nom-lieu');
    const suggestions = conteneur.querySelector('.suggestions');
    let lieuActuel = depart && depart.lieu ? depart.lieu : '';
    let minuteur;

    // Le viseur est dessiné un peu au-dessus du centre, pour laisser la place au panneau du bas
    const pointVise = () => {
      const taille = carte.getSize();
      return carte.containerPointToLatLng([taille.x / 2, taille.y / 2 - 60]);
    };
    const nommer = async () => {
      const p = pointVise();
      nomLieu.textContent = 'Recherche de l\'adresse…';
      try {
        const r = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&accept-language=fr&zoom=17&lat=${p.lat}&lon=${p.lng}`);
        const d = await r.json();
        const a = d.address || {};
        lieuActuel = [a.road || a.amenity || a.building || a.neighbourhood, a.town || a.city || a.village || a.county]
          .filter(Boolean).join(', ') || (d.display_name || '').split(',').slice(0, 2).join(',');
      } catch (e) {
        lieuActuel = `Point ${p.lat.toFixed(4)}, ${p.lng.toFixed(4)}`;
      }
      nomLieu.textContent = lieuActuel || 'Lieu sans nom';
    };
    carte.on('moveend', () => { clearTimeout(minuteur); minuteur = setTimeout(nommer, 400); });
    if (lieuActuel) nomLieu.textContent = lieuActuel; else nommer();

    conteneur.querySelector('#champ-lieu').addEventListener('input', e => {
      clearTimeout(minuteur);
      const q = e.target.value.trim();
      if (q.length < 3) { suggestions.hidden = true; return; }
      minuteur = setTimeout(async () => {
        try {
          const r = await fetch(`https://nominatim.openstreetmap.org/search?format=json&countrycodes=tn&accept-language=fr&limit=5&q=${encodeURIComponent(q)}`);
          const resultats = await r.json();
          suggestions.innerHTML = resultats.map(x =>
            `<button data-lat="${x.lat}" data-lon="${x.lon}">${echapper(x.display_name)}</button>`).join('')
            || '<button disabled>Aucun lieu trouvé</button>';
          suggestions.hidden = false;
        } catch (err) { suggestions.hidden = true; }
      }, 400);
    });
    suggestions.addEventListener('click', e => {
      const b = e.target.closest('button[data-lat]');
      if (!b) return;
      suggestions.hidden = true;
      carte.setView([+b.dataset.lat, +b.dataset.lon], 17);
    });

    const fermer = valeur => { carte.remove(); conteneur.remove(); resolve(valeur); };
    conteneur.addEventListener('click', e => {
      const action = e.target.closest('[data-action]')?.dataset.action;
      if (action === 'fermer') fermer(null);
      if (action === 'position') {
        if (!navigator.geolocation) return toast('La géolocalisation n\'est pas disponible.', 'erreur');
        navigator.geolocation.getCurrentPosition(
          pos => carte.setView([pos.coords.latitude, pos.coords.longitude], 17),
          () => toast('Position refusée ou indisponible.', 'erreur'));
      }
      if (action === 'confirmer') {
        const p = pointVise();
        fermer({ lieu: lieuActuel || nomLieu.textContent, latitude: p.lat, longitude: p.lng });
      }
    });
  });
}


// ---------- Notes (étoiles) ----------

function noteTexte(n) {
  return Number(n || 0).toFixed(1).replace('.', ',');
}

/** Cinq étoiles, remplies selon la note (arrondie à la demi-étoile près vers le bas). */
function etoiles(note, classe = '') {
  const pleines = Math.round(Number(note || 0));
  let html = '';
  for (let i = 1; i <= 5; i++) html += `<span class="${i <= pleines ? 'pleine' : ''}">${ic('etoile')}</span>`;
  return `<span class="etoiles ${classe}" role="img" aria-label="Note ${noteTexte(note)} sur 5">${html}</span>`;
}

/** Pastille d'état d'une réservation. */
function etatReservation(r) {
  if (r.statut === 'EN_ATTENTE') return '<span class="etat jaune">En attente du conducteur</span>';
  if (r.statut === 'REFUSEE') return '<span class="etat rouge">Refusée</span>';
  if (r.statut === 'ANNULEE' && r.trajet.annule) return '<span class="etat rouge">Trajet annulé par le conducteur</span>';
  if (r.statut === 'ANNULEE') return '<span class="etat">Annulée</span>';
  if (r.statut === 'EXPIREE') return '<span class="etat">Expirée sans réponse</span>';
  return estPasse(r.trajet) ? '<span class="etat bleu">Effectué</span>' : '<span class="etat ok">Confirmée</span>';
}

// ---------- Notifications (user story #9) ----------

const ICONE_NOTIF = {
  DEMANDE_RESERVATION: 'billet', RESERVATION_ACCEPTEE: 'ok', RESERVATION_REFUSEE: 'croix',
  RESERVATION_ANNULEE: 'alerte', RESERVATION_EXPIREE: 'horloge', TRAJET_ANNULE: 'croix', NOUVEAU_SIGNALEMENT: 'alerte', NOUVEAU_MESSAGE: 'message', RAPPEL_DEPART: 'horloge', NOUVEL_AVIS: 'etoile'
};

function brancherNotifications(haut, u) {
  const bouton = haut.querySelector('.cloche');
  const pastille = haut.querySelector('.pastille');
  const panneau = haut.querySelector('.panneau-notifs');

  const compter = async () => {
    try {
      const { nombre } = await api(`/notifications/${u.id}/non-lues`);
      pastille.hidden = !nombre;
      pastille.textContent = nombre > 9 ? '9+' : nombre;
      bouton.setAttribute('aria-label', nombre ? `Notifications, ${nombre} non lue${nombre > 1 ? 's' : ''}` : 'Notifications');
    } catch (e) { /* serveur arrêté : on réessaiera */ }
  };

  const ouvrir = async () => {
    panneau.hidden = false;
    bouton.setAttribute('aria-expanded', 'true');
    panneau.innerHTML = `<div class="tete-notifs"><strong>Notifications</strong></div>${chargement(2)}`;
    try {
      const notifs = await api('/notifications/' + u.id);
      panneau.innerHTML = `<div class="tete-notifs"><strong>Notifications</strong></div>` + (notifs.length
        ? notifs.map(n => `
          <a class="notif ${n.lue ? '' : 'nouvelle'}" href="${echapper(n.lien || '#')}">
            <span class="rond ${n.type.toLowerCase()}">${ic(ICONE_NOTIF[n.type] || 'cloche')}</span>
            <span class="texte"><strong>${echapper(n.titre)}</strong><span>${echapper(n.message)}</span>
              <time>${formaterMoment(n.dateCreation)}</time></span>
          </a>`).join('')
        : etatVide('cloche', 'Rien de nouveau', 'Les réponses à vos demandes et les nouveaux messages arriveront ici.'));
      if (notifs.some(n => !n.lue)) {
        await api(`/notifications/${u.id}/lues`, { method: 'PUT' });
        pastille.hidden = true;
      }
    } catch (err) {
      panneau.innerHTML = etatVide('alerte', 'Notifications indisponibles', echapper(err.message));
    }
  };
  const fermer = () => { panneau.hidden = true; bouton.setAttribute('aria-expanded', 'false'); };

  bouton.addEventListener('click', e => { e.stopPropagation(); panneau.hidden ? ouvrir() : fermer(); });
  document.addEventListener('click', e => { if (!panneau.hidden && !e.target.closest('.cloche-zone')) fermer(); });
  document.addEventListener('keydown', e => { if (e.key === 'Escape' && !panneau.hidden) { fermer(); bouton.focus(); } });

  compter();
  setInterval(() => { if (!document.hidden) compter(); }, 30000);
}

// ---------- Assistant (user story #10) ----------

function installerAssistant(u) {
  if (document.querySelector('.assistant')) return;
  const zone = document.createElement('div');
  zone.className = 'assistant';
  zone.innerHTML = `
    <button class="bouton-assistant" aria-label="Ouvrir l'assistant" aria-expanded="false">${ic('assistant')}<span>Assistant</span></button>
    <section class="fenetre-assistant" role="dialog" aria-label="Assistant Covoit" hidden>
      <header>
        <span class="plaque-logo">${ic('assistant')}</span>
        <div><strong>Assistant Covoit</strong><span>Trajets, réservations, questions</span></div>
        <button class="btn-icone fermer-assistant" aria-label="Fermer l'assistant">${ic('croix')}</button>
      </header>
      <div class="fil-assistant" aria-live="polite"></div>
      <div class="puces"></div>
      <form>
        <input name="q" maxlength="300" autocomplete="off" placeholder="Ex. : un trajet de Tunis demain matin" aria-label="Votre question">
        <button class="btn" aria-label="Envoyer">${ic('envoyer')}</button>
      </form>
    </section>`;
  document.body.appendChild(zone);

  const bouton = zone.querySelector('.bouton-assistant');
  const fenetre = zone.querySelector('.fenetre-assistant');
  const fil = zone.querySelector('.fil-assistant');
  const puces = zone.querySelector('.puces');
  const form = zone.querySelector('form');
  const champ = form.q;
  let historique = [];
  try { historique = JSON.parse(sessionStorage.getItem('assistant-' + u.id)) || []; } catch (e) { historique = []; }

  const garder = () => { try { sessionStorage.setItem('assistant-' + u.id, JSON.stringify(historique.slice(-20))); } catch (e) { /* rien */ } };

  const bulle = m => {
    if (m.de === 'moi') return `<div class="msg moi">${echapper(m.texte)}</div>`;
    const trajets = (m.trajets || []).map(t => `
      <a class="mini-trajet" href="trajet.html?id=${t.id}">
        <strong>${echapper(ville(t.villeDepart))} → ${echapper(ville(t.villeArrivee))}</strong>
        <span>${formaterJourCourt(t.dateDepart)} à ${formaterHeure(t.heureDepart)} · ${formaterPrix(t.prix)} · ${t.placesDisponibles} pl.</span>
      </a>`).join('');
    return `<div class="msg">${echapper(m.texte).replace(/\n/g, '<br>')}${trajets ? `<div class="mini-trajets">${trajets}</div>` : ''}</div>`;
  };
  const afficher = () => {
    fil.innerHTML = historique.map(bulle).join('');
    fil.scrollTop = fil.scrollHeight;
  };
  const proposer = liste => {
    puces.innerHTML = (liste || []).map(s => `<button type="button">${echapper(s)}</button>`).join('');
  };

  const envoyer = async texte => {
    texte = texte.trim();
    if (!texte) return;
    historique.push({ de: 'moi', texte });
    afficher();
    proposer([]);
    fil.insertAdjacentHTML('beforeend', '<div class="msg ecrit" aria-label="L\'assistant écrit"><i></i><i></i><i></i></div>');
    fil.scrollTop = fil.scrollHeight;
    try {
      const r = await api('/assistant', { method: 'POST', body: { utilisateurId: u.id, message: texte } });
      historique.push({ de: 'bot', texte: r.reponse, trajets: r.trajets });
      proposer(r.suggestions);
    } catch (err) {
      historique.push({ de: 'bot', texte: err.message });
    }
    garder();
    afficher();
  };

  const ouvrir = () => {
    fenetre.hidden = false;
    zone.classList.add('ouvert');
    bouton.setAttribute('aria-expanded', 'true');
    if (!historique.length) {
      historique.push({ de: 'bot', texte: `Bonjour ${u.prenom} ! Dites-moi où vous voulez aller, par exemple « un trajet de Ben Arous vers l'ISET lundi à 8h ».` });
      proposer(estConducteur(u)
        ? ['Mes demandes en attente', "Un trajet vers l'ISET demain", 'Mes réservations']
        : ["Un trajet vers l'ISET demain", 'Mes réservations', 'Comment annuler ?']);
    }
    afficher();
    champ.focus();
  };
  const fermer = () => {
    fenetre.hidden = true;
    zone.classList.remove('ouvert');
    bouton.setAttribute('aria-expanded', 'false');
    bouton.focus();
  };

  bouton.addEventListener('click', ouvrir);
  zone.querySelector('.fermer-assistant').addEventListener('click', fermer);
  fenetre.addEventListener('keydown', e => { if (e.key === 'Escape') fermer(); });
  puces.addEventListener('click', e => { const b = e.target.closest('button'); if (b) envoyer(b.textContent); });
  form.addEventListener('submit', e => { e.preventDefault(); const t = champ.value; champ.value = ''; envoyer(t); });
}


// ---------- Sécurité entre utilisateurs : signaler, bloquer ----------

const MOTIFS = {
  COMPORTEMENT: 'Propos ou comportement déplacés',
  CONDUITE_DANGEREUSE: 'Conduite dangereuse',
  ABSENCE: 'Ne s\'est pas présenté(e)',
  FAUX_PROFIL: 'Faux profil',
  AUTRE: 'Autre'
};

/** Boutons « Signaler » et « Bloquer » pour le profil d'une autre personne. */
function boutonsSecurite(cibleId, nom) {
  return `<div class="rangee securite" data-cible="${cibleId}" data-nom="${echapper(nom)}">
    <button class="btn contour petit" data-action="signaler">${ic('alerte')} Signaler</button>
    <button class="btn contour petit" data-action="bloquer">${ic('croix')} Bloquer</button>
  </div>`;
}

/** Met à jour le bouton Bloquer / Débloquer selon la liste de l'utilisateur connecté. */
async function brancherSecurite(conteneur, trajetId = null) {
  const zone = conteneur.querySelector('.securite');
  if (!zone) return;
  const u = utilisateurConnecte();
  const cible = Number(zone.dataset.cible);
  const nom = zone.dataset.nom;
  const boutonBloquer = zone.querySelector('[data-action="bloquer"]');
  let bloque = false;
  const peindre = () => { boutonBloquer.innerHTML = bloque ? ic('ok') + ' Débloquer' : ic('croix') + ' Bloquer'; };
  try { bloque = (await api('/blocages/' + u.id)).some(b => b.utilisateurId === cible); } catch (e) { /* rien */ }
  peindre();

  boutonBloquer.onclick = async () => {
    if (!bloque && !(await confirmer(`Bloquer ${nom} ?`,
      'Vous ne pourrez plus vous écrire, et aucun de vous deux ne pourra réserver dans les trajets de l\'autre. Il ou elle n\'est pas prévenu(e).',
      'Bloquer', true))) return;
    try {
      if (bloque) await api(`/blocages?bloqueurId=${u.id}&bloqueId=${cible}`, { method: 'DELETE' });
      else await api('/blocages', { method: 'POST', body: { bloqueurId: u.id, bloqueId: cible } });
      bloque = !bloque;
      peindre();
      toast(bloque ? `${nom} est bloqué(e)` : `${nom} est débloqué(e)`);
    } catch (err) { toast(err.message, 'erreur'); }
  };
  zone.querySelector('[data-action="signaler"]').onclick = () => signaler(cible, nom, trajetId);
}

function signaler(cibleId, nom, trajetId = null) {
  const u = utilisateurConnecte();
  const { element, fermer } = ouvrirFenetre(`
    <h3>Signaler ${echapper(nom)}</h3>
    <p class="chapeau">L'administration de l'ISET examinera votre signalement. La personne n'est pas informée que c'est vous.</p>
    <label class="etiquette" for="motif">Motif</label>
    <div class="entree"><select id="motif">${Object.entries(MOTIFS).map(([k, v]) => `<option value="${k}">${v}</option>`).join('')}</select></div>
    <label class="etiquette" for="details" style="margin-top:14px">Que s'est-il passé ? (facultatif)</label>
    <textarea class="zone" id="details" maxlength="1000" placeholder="Date, trajet, ce qui s'est passé…"></textarea>
    <p class="aide">En cas de danger immédiat, appelez la police (197) ou la Protection civile (198).</p>
    <div class="rangee" style="justify-content:flex-end;margin-top:16px">
      <button class="btn contour" data-r="non">Annuler</button>
      <button class="btn danger" data-r="oui">Envoyer le signalement</button>
    </div>`);
  element.querySelector('[data-r="non"]').onclick = fermer;
  element.querySelector('[data-r="oui"]').onclick = async () => {
    try {
      await api('/signalements', { method: 'POST', body: {
        auteurId: u.id, signaleId: cibleId, trajetId,
        motif: element.querySelector('#motif').value,
        details: element.querySelector('#details').value.trim() || null } });
      fermer();
      toast('Signalement envoyé. Merci, l\'administration va l\'examiner.');
    } catch (err) { toast(err.message, 'erreur'); }
  };
}
