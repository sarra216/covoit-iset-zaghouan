package tn.iset.covoit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthControllerTest {

    @Test
    void quandLaBaseRepondLeStatutEstOk() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connexion = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connexion);
        when(connexion.isValid(anyInt())).thenReturn(true);

        ResponseEntity<Map<String, String>> reponse = new HealthController(dataSource, "a1b2c3d").sante();

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertEquals(HealthController.STATUT_OK, reponse.getBody().get("status"));
        assertEquals("a1b2c3d", reponse.getBody().get("version"));
    }

    @Test
    void quandLaBaseEstInjoignableLeStatutEstDown() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("Connexion refusée"));

        ResponseEntity<Map<String, String>> reponse = new HealthController(dataSource, "dev").sante();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, reponse.getStatusCode());
        assertEquals("down", reponse.getBody().get("status"));
        assertEquals("erreur", reponse.getBody().get("base"));
    }
}
