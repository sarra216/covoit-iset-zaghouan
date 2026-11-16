package tn.iset.covoit.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base des tests de bout en bout (« end-to-end ») avec Selenium :
 * un vrai Chrome ouvre le site et clique comme un utilisateur.
 *
 * Prérequis : l'application tourne (CovoitApplication) et Google Chrome est installé.
 * Selenium télécharge lui-même le bon « chromedriver » au premier lancement (Internet nécessaire).
 *
 * Réglages (Run Configurations › Arguments › VM arguments) :
 *   -De2e.url=http://localhost:8082   adresse de l'application (8082 par défaut)
 *   -De2e.headless=true               sans fenêtre visible (plus rapide)
 *   -De2e.lent=true                   pause d'une demi-seconde entre les actions (pour une démonstration)
 *
 * Ces tests portent le tag « e2e » : ils ne tournent pas avec « mvn test » ni dans GitHub Actions.
 * Pour les lancer : clic droit sur la classe › Run As › JUnit Test, ou « mvn test -Pe2e ».
 */
@Tag("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(CaptureSiEchec.class)
public abstract class NavigateurE2E {

    protected static final String URL = System.getProperty("e2e.url", "http://localhost:8082").replaceAll("/+$", "");
    private static final boolean LENT = Boolean.getBoolean("e2e.lent");

    protected WebDriver navigateur;
    protected WebDriverWait attente;

    @BeforeAll
    void ouvrirNavigateur() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.getBoolean("e2e.headless")) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900", "--lang=fr-FR");
        navigateur = new ChromeDriver(options);
        attente = new WebDriverWait(navigateur, Duration.ofSeconds(10));
    }

    @AfterAll
    void fermerNavigateur() {
        if (navigateur != null) {
            navigateur.quit();
        }
    }

    WebDriver navigateur() {
        return navigateur;
    }

    // ---------- Actions de base ----------

    protected void ouvrir(String page) {
        navigateur.get(URL + "/" + page);
        pause();
    }

    protected WebElement element(By selecteur) {
        return attente.until(ExpectedConditions.visibilityOfElementLocated(selecteur));
    }

    protected void cliquer(By selecteur) {
        attente.until(ExpectedConditions.elementToBeClickable(selecteur)).click();
        pause();
    }

    /** Pour les cases à cocher stylisées (l'input réel est transparent) : clic en JavaScript. */
    protected void cliquerJs(By selecteur) {
        WebElement e = attente.until(ExpectedConditions.presenceOfElementLocated(selecteur));
        js("arguments[0].click();", e);
        pause();
    }

    protected void saisir(String id, String texte) {
        WebElement champ = element(By.id(id));
        champ.clear();
        champ.sendKeys(texte);
    }

    protected Object js(String script, Object... arguments) {
        return ((JavascriptExecutor) navigateur).executeScript(script, arguments);
    }

    protected void attendreUrl(String morceau) {
        attente.until(ExpectedConditions.urlContains(morceau));
    }

    /** Attend un message éphémère (toast) contenant ce texte et renvoie son contenu complet. */
    protected String attendreToast(String texte) {
        return attente.until(d -> d.findElements(By.cssSelector(".toast")).stream()
                .map(WebElement::getText)
                .filter(t -> t.contains(texte))
                .findFirst()
                .orElse(null));
    }

    /** Attend un élément CSS dont le texte contient « texte ». */
    protected WebElement elementContenant(String css, String texte) {
        return attente.until(d -> d.findElements(By.cssSelector(css)).stream()
                .filter(e -> e.getText().contains(texte))
                .findFirst()
                .orElse(null));
    }

    /** Clique sur le bouton de confirmation de la fenêtre modale ouverte. */
    protected void confirmerFenetre() {
        cliquer(By.cssSelector(".fenetre [data-r='oui']"));
    }

    // ---------- Parcours fréquents ----------

    protected void viderSession() {
        ouvrir("index.html");
        js("localStorage.clear();");
    }

    protected void connecter(String email, String motDePasse) {
        viderSession();
        ouvrir("index.html");
        saisir("c-email", email);
        saisir("c-mdp", motDePasse);
        cliquer(By.cssSelector("#form-connexion button[type='submit']"));
        attendreUrl("recherche.html");
    }

    protected void inscrire(String prenom, String nom, String email, String telephone, String motDePasse) {
        viderSession();
        ouvrir("index.html");
        cliquer(By.id("onglet-inscription"));
        saisir("i-prenom", prenom);
        saisir("i-nom", nom);
        saisir("i-email", email);
        saisir("i-tel", telephone);
        saisir("i-mdp", motDePasse);
        cliquer(By.id("i-conditions"));
        cliquer(By.cssSelector("#form-inscription button[type='submit']"));
        attendreUrl("completer-profil.html");
    }

    private void pause() {
        if (!LENT) return;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
