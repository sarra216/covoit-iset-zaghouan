package tn.iset.covoit.e2e;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Quand un test Selenium échoue, une capture d'écran du navigateur est enregistrée dans
 * target/e2e-captures/<Classe>-<test>.png : on voit exactement ce qui était affiché.
 */
public class CaptureSiEchec implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext contexte, Throwable cause) {
        Object test = contexte.getTestInstance().orElse(null);
        if (!(test instanceof NavigateurE2E e2e) || e2e.navigateur() == null) return;
        WebDriver navigateur = e2e.navigateur();
        try {
            File capture = ((TakesScreenshot) navigateur).getScreenshotAs(OutputType.FILE);
            Path dossier = Path.of("target", "e2e-captures");
            Files.createDirectories(dossier);
            Path cible = dossier.resolve(contexte.getRequiredTestClass().getSimpleName() + "-"
                    + contexte.getRequiredTestMethod().getName() + ".png");
            Files.copy(capture.toPath(), cible, StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Capture d'écran de l'échec : " + cible.toAbsolutePath()
                    + "\nPage affichée : " + navigateur.getCurrentUrl());
        } catch (Exception e) {
            System.err.println("Capture d'écran impossible : " + e.getMessage());
        }
    }
}
