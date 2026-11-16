package tn.iset.covoit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.iset.covoit.Donnees;
import tn.iset.covoit.dto.ConnexionRequest;
import tn.iset.covoit.dto.InscriptionRequest;
import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock UtilisateurRepository utilisateurRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CompteService compteService;

    Utilisateur sara;

    @BeforeEach
    void preparer() {
        sara = Donnees.utilisateur(2, "Sara", Role.PASSAGER);
        sara.setMotDePasse("hash");
        when(utilisateurRepository.findByEmail("sara@iset.tn")).thenReturn(Optional.of(sara));
        when(passwordEncoder.matches("bon-mot-de-passe", "hash")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(appel -> appel.getArgument(0));
    }

    private AuthService service(boolean verification, String domaines) {
        return new AuthService(utilisateurRepository, passwordEncoder, compteService, new LimiteurConnexion(),
                verification, domaines);
    }

    private InscriptionRequest inscription(String email) {
        return new InscriptionRequest("Test", "Ali", email, "secret123", "22123456", Role.PASSAGER, true);
    }

    @Test
    void connexionAvecLeBonMotDePasse() {
        assertEquals(2L, service(false, "").connecter(new ConnexionRequest("Sara@ISET.tn ", "bon-mot-de-passe")).id());
    }

    @Test
    void mauvaisMotDePasseDonne401() {
        ApiException e = assertThrows(ApiException.class,
                () -> service(false, "").connecter(new ConnexionRequest("sara@iset.tn", "faux")));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void unCompteSuspenduNePeutPasSeConnecter() {
        sara.setSuspendu(true);

        ApiException e = assertThrows(ApiException.class,
                () -> service(false, "").connecter(new ConnexionRequest("sara@iset.tn", "bon-mot-de-passe")));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void avecLaVerificationActiveUnEmailNonConfirmeEstRefuse() {
        sara.setEmailVerifie(false);

        assertThrows(ApiException.class,
                () -> service(true, "").connecter(new ConnexionRequest("sara@iset.tn", "bon-mot-de-passe")));
        assertDoesNotThrow(() -> service(false, "").connecter(new ConnexionRequest("sara@iset.tn", "bon-mot-de-passe")));
    }

    @Test
    void apresCinqEchecsMemeLeBonMotDePasseEstBloque() {
        AuthService auth = service(false, "");
        for (int i = 0; i < 5; i++) {
            assertThrows(ApiException.class, () -> auth.connecter(new ConnexionRequest("sara@iset.tn", "faux")));
        }
        ApiException e = assertThrows(ApiException.class,
                () -> auth.connecter(new ConnexionRequest("sara@iset.tn", "bon-mot-de-passe")));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatus());
    }

    @Test
    void seulsLesDomainesAutorisesPeuventSInscrire() {
        AuthService auth = service(false, "iset-zaghouan.tn, isetz.tn");

        assertThrows(ApiException.class, () -> auth.inscrire(inscription("ali@gmail.com")));
        assertDoesNotThrow(() -> auth.inscrire(inscription("ali@isetz.tn")));
    }

    @Test
    void lInscriptionEnvoieLEmailDeVerificationSiActive() {
        service(true, "").inscrire(inscription("ali@iset.tn"));

        verify(compteService).envoyerVerification(argThat(u -> !u.isEmailVerifie()
                && u.getDateAcceptationConditions() != null));
    }

    @Test
    void onNePeutPasSInscrireCommeAdmin() {
        InscriptionRequest admin = new InscriptionRequest("Test", "Ali", "ali@iset.tn", "secret123", "22123456", Role.ADMIN, true);

        assertThrows(ApiException.class, () -> service(false, "").inscrire(admin));
    }
}
