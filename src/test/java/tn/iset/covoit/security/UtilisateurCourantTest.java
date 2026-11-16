package tn.iset.covoit.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.iset.covoit.exception.ApiException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilisateurCourantTest {

    private final UtilisateurCourant courant = new UtilisateurCourant();

    private void connecter(long id) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(id, null, List.of()));
    }

    @AfterEach
    void deconnecter() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void lUtilisateurPeutAgirPourLuiMeme() {
        connecter(7L);

        assertEquals(7L, courant.id());
        assertDoesNotThrow(() -> courant.verifier(7L));
    }

    @Test
    void lUtilisateurNePeutPasAgirAuNomDUnAutre() {
        connecter(7L);

        ApiException e = assertThrows(ApiException.class, () -> courant.verifier(8L));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void sansJetonLaRequeteEstRefusee() {
        ApiException e = assertThrows(ApiException.class, courant::id);
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }

    @Test
    void uneConversationNeConcerneQueSesDeuxParticipants() {
        connecter(7L);

        assertDoesNotThrow(() -> courant.verifierUnDe(3L, 7L));
        assertThrows(ApiException.class, () -> courant.verifierUnDe(3L, 4L));
    }
}
