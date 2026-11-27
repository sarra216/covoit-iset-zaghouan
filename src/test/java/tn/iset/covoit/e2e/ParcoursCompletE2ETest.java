package tn.iset.covoit.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le parcours principal de l'application, de bout en bout, dans un vrai navigateur :
 * un conducteur et un passager s'inscrivent, le conducteur publie un trajet, le passager le trouve
 * et demande une place, le conducteur accepte, puis annule le trajet et le passager est prévenu.
 * À la fin, les deux comptes de test sont supprimés (ce qui teste aussi la suppression de compte).
 *
 * Chaque lancement crée des comptes neufs (emails uniques) : le test ne dépend pas des données existantes.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Parcours complet : inscription → trajet → demande → acceptation → annulation")
class ParcoursCompletE2ETest extends NavigateurE2E {

    static final long NUMERO = System.currentTimeMillis() % 10_000_000;
    static final String SUFFIXE = String.valueOf(NUMERO);
    static final String MOT_DE_PASSE = "Selenium2026";

    static final String NOM_CONDUCTEUR = "Conducteur" + SUFFIXE;
    static final String EMAIL_CONDUCTEUR = "conducteur" + SUFFIXE + "@test.iset.tn";
    static final String NOM_PASSAGER = "Passager" + SUFFIXE;
    static final String EMAIL_PASSAGER = "passager" + SUFFIXE + "@test.iset.tn";

    static long trajetId;

    @Test
    @Order(1)
    @DisplayName("1. Le conducteur s'inscrit et enregistre sa voiture")
    void inscrireLeConducteur() {
        inscrire("Selenium", NOM_CONDUCTEUR, EMAIL_CONDUCTEUR, telephone(1), MOT_DE_PASSE);

        // Page « Compléter le profil » : activer le mode conducteur et décrire la voiture
        cliquerJs(By.id("conducteur"));
        saisir("modele", "Renault Clio");
        saisir("couleur", "Gris");
        saisir("serie", "123");
        saisir("numero", "4567");
        cliquer(By.cssSelector("#form-profil button[type='submit']"));

        attendreToast("Profil enregistré");
        attendreUrl("recherche.html");
    }

    @Test
    @Order(2)
    @DisplayName("2. Le passager s'inscrit")
    void inscrireLePassager() {
        inscrire("Selenium", NOM_PASSAGER, EMAIL_PASSAGER, telephone(2), MOT_DE_PASSE);

        assertTrue(navigateur.getCurrentUrl().contains("completer-profil.html"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Le conducteur publie un trajet Tunis → ISET Zaghouan pour demain")
    void publierUnTrajet() {
        connecter(EMAIL_CONDUCTEUR, MOT_DE_PASSE);
        ouvrir("proposer.html");
        saisir("depart", "Tunis");
        saisir("arrivee", "ISET Zaghouan");
        // Un champ date se remplit différemment selon la langue du navigateur : on passe par JavaScript
        js("const d = document.getElementById('date'); d.value = arguments[0];"
                + "d.dispatchEvent(new Event('input', {bubbles: true})); d.dispatchEvent(new Event('change', {bubbles: true}));",
                LocalDate.now().plusDays(1).toString());
        cliquer(By.id("publier"));

        attendreToast("Trajet publié");
        attendreUrl("onglet=proposes");
        String lien = element(By.cssSelector(".ligne-trajet a.itineraire")).getAttribute("href");
        Matcher m = Pattern.compile("id=(\\d+)").matcher(lien);
        assertTrue(m.find(), "lien du trajet : " + lien);
        trajetId = Long.parseLong(m.group(1));
    }

    @Test
    @Order(4)
    @DisplayName("4. Le passager trouve le trajet et demande une place")
    void demanderUnePlace() {
        connecter(EMAIL_PASSAGER, MOT_DE_PASSE);
        saisir("depart", "Tunis");
        saisir("arrivee", "ISET Zaghouan");
        cliquer(By.id("btn-rechercher"));

        WebElement ligne = elementContenant(".ligne-trajet", NOM_CONDUCTEUR);
        ligne.findElement(By.cssSelector("a.itineraire")).click();
        attendreUrl("trajet.html?id=" + trajetId);

        cliquer(By.id("reserver"));
        confirmerFenetre();

        attendreToast("Demande envoyée");
        assertTrue(elementContenant(".alerte", "Demande envoyée").isDisplayed());
    }

    @Test
    @Order(5)
    @DisplayName("5. Le conducteur reçoit la demande et l'accepte")
    void accepterLaDemande() {
        connecter(EMAIL_CONDUCTEUR, MOT_DE_PASSE);
        ouvrir("reservations.html?onglet=demandes");

        WebElement demande = elementContenant(".demande", NOM_PASSAGER);
        demande.findElement(By.cssSelector(".btn.vert")).click();

        attendreToast("Demande acceptée");
    }

    @Test
    @Order(6)
    @DisplayName("6. Le passager voit sa réservation confirmée et la notification")
    void reservationConfirmee() {
        connecter(EMAIL_PASSAGER, MOT_DE_PASSE);
        ouvrir("reservations.html");
        WebElement ligne = elementContenant(".ligne-trajet", NOM_CONDUCTEUR);
        assertTrue(ligne.getText().contains("Confirmée"), "statut affiché : " + ligne.getText());

        cliquer(By.cssSelector(".cloche"));
        elementContenant(".notif", "Réservation acceptée");
    }

    @Test
    @Order(7)
    @DisplayName("7. L'assistant retrouve le trajet à partir d'une phrase")
    void assistant() {
        ouvrir("recherche.html");
        cliquer(By.cssSelector(".bouton-assistant"));
        WebElement champ = element(By.cssSelector(".fenetre-assistant input"));
        champ.sendKeys("un trajet de Tunis vers l'ISET demain", Keys.ENTER);

        elementContenant(".msg", "J'ai trouvé");
        assertFalse(navigateur.findElements(By.cssSelector(".mini-trajet")).isEmpty());
    }

    @Test
    @Order(8)
    @DisplayName("8. Le conducteur annule son trajet")
    void annulerLeTrajet() {
        connecter(EMAIL_CONDUCTEUR, MOT_DE_PASSE);
        ouvrir("trajet.html?id=" + trajetId);
        cliquer(By.id("annuler-trajet"));
        confirmerFenetre();

        attendreToast("Trajet annulé");
        elementContenant(".alerte", "annulé par le conducteur");
        assertTrue(navigateur.findElements(By.id("annuler-trajet")).isEmpty(), "le bouton Annuler doit disparaître");
    }

    @Test
    @Order(9)
    @DisplayName("9. Le passager est prévenu de l'annulation")
    void passagerPrevenu() {
        connecter(EMAIL_PASSAGER, MOT_DE_PASSE);
        cliquer(By.cssSelector(".cloche"));
        elementContenant(".notif", "Trajet annulé");

        ouvrir("reservations.html");
        assertTrue(elementContenant(".ligne-trajet", NOM_CONDUCTEUR).getText().contains("annulé"));
    }

    @Test
    @Order(10)
    @DisplayName("10. Les deux comptes de test sont supprimés")
    void supprimerLesComptes() {
        for (String email : new String[]{EMAIL_PASSAGER, EMAIL_CONDUCTEUR}) {
            connecter(email, MOT_DE_PASSE);
            ouvrir("profil.html");
            cliquer(By.id("supprimer"));
            saisir("mdp-suppression", MOT_DE_PASSE);
            confirmerFenetre();
            attendreUrl("index.html");
        }

        // Le compte n'existe plus : la connexion est refusée
        viderSession();
        ouvrir("index.html");
        saisir("c-email", EMAIL_PASSAGER);
        saisir("c-mdp", MOT_DE_PASSE);
        cliquer(By.cssSelector("#form-connexion button[type='submit']"));
        attendreToast("incorrect");
    }

    /** Numéro tunisien à 8 chiffres, différent à chaque lancement. */
    private static String telephone(int n) {
        return n + String.format("%07d", NUMERO % 10_000_000);
    }
}
