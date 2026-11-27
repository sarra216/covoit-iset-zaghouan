package tn.iset.covoit.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;

import static org.junit.jupiter.api.Assertions.*;

/** Vérifications rapides sur les pages publiques, la connexion et la protection des pages. */
@DisplayName("Connexion, pages publiques et sécurité")
class ConnexionEtSecuriteE2ETest extends NavigateurE2E {

    @Test
    @DisplayName("Un mauvais mot de passe affiche un message d'erreur")
    void mauvaisMotDePasse() {
        viderSession();
        ouvrir("index.html");
        saisir("c-email", "personne-" + System.currentTimeMillis() + "@test.iset.tn");
        saisir("c-mdp", "mauvais-mot-de-passe");
        cliquer(By.cssSelector("#form-connexion button[type='submit']"));

        attendreToast("Email ou mot de passe incorrect");
        assertTrue(navigateur.getCurrentUrl().contains("index.html"));
    }

    @Test
    @DisplayName("Une page privée sans session renvoie vers la connexion")
    void pagePriveeSansSession() {
        viderSession();
        ouvrir("reservations.html");

        attendreUrl("index.html");
    }

    @Test
    @DisplayName("Un jeton falsifié est refusé et la session est effacée")
    void jetonFalsifie() {
        viderSession();
        js("localStorage.setItem('utilisateur', JSON.stringify({id: 1, prenom: 'Pirate', nom: 'Test', role: 'ADMIN'}));"
                + "localStorage.setItem('jeton', 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.faux');");
        ouvrir("reservations.html");

        attendreUrl("index.html?expire=1");
        assertNull(js("return localStorage.getItem('jeton');"));
    }

    @Test
    @DisplayName("L'inscription est impossible sans accepter les conditions")
    void inscriptionSansConditions() {
        viderSession();
        ouvrir("index.html");
        cliquer(By.id("onglet-inscription"));
        saisir("i-prenom", "Test");
        saisir("i-nom", "Conditions");
        saisir("i-email", "conditions-" + System.currentTimeMillis() + "@test.iset.tn");
        saisir("i-tel", "21234567");
        saisir("i-mdp", "secret123");
        cliquer(By.cssSelector("#form-inscription button[type='submit']"));

        assertEquals(Boolean.TRUE, js("return document.getElementById('i-conditions').validity.valueMissing;"));
        assertTrue(navigateur.getCurrentUrl().contains("index.html"), "on reste sur la page d'inscription");
    }

    @Test
    @DisplayName("Mot de passe oublié : le formulaire confirme l'envoi")
    void motDePasseOublie() {
        viderSession();
        ouvrir("index.html");
        cliquer(By.linkText("Mot de passe oublié ?"));
        attendreUrl("mot-de-passe-oublie.html");
        saisir("email", "quelquun@test.iset.tn");
        cliquer(By.cssSelector("#formulaire button[type='submit']"));

        assertTrue(element(By.id("envoye")).getText().contains("Regardez vos emails"));
    }

    @Test
    @DisplayName("Les conditions d'utilisation et la confidentialité sont publiques")
    void pagesLegales() {
        viderSession();
        ouvrir("conditions.html");
        assertEquals("Conditions d'utilisation", element(By.tagName("h1")).getText());

        ouvrir("confidentialite.html");
        assertEquals("Politique de confidentialité", element(By.tagName("h1")).getText());
    }

    @Test
    @DisplayName("Sur téléphone, la page de connexion reste utilisable")
    void affichageTelephone() {
        navigateur.manage().window().setSize(new Dimension(390, 844));
        try {
            viderSession();
            ouvrir("index.html");
            assertTrue(element(By.cssSelector("#form-connexion button[type='submit']")).isDisplayed());
            Long largeurPage = (Long) js("return document.documentElement.scrollWidth;");
            Long largeurEcran = (Long) js("return window.innerWidth;");
            assertTrue(largeurPage <= largeurEcran, "pas de défilement horizontal (" + largeurPage + " > " + largeurEcran + ")");
        } finally {
            navigateur.manage().window().setSize(new Dimension(1366, 900));
        }
    }
}
