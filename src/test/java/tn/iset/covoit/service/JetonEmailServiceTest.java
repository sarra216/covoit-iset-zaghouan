package tn.iset.covoit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.iset.covoit.Donnees;
import tn.iset.covoit.entity.JetonEmail;
import tn.iset.covoit.entity.Role;
import tn.iset.covoit.entity.TypeJeton;
import tn.iset.covoit.entity.Utilisateur;
import tn.iset.covoit.exception.ApiException;
import tn.iset.covoit.repository.JetonEmailRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JetonEmailServiceTest {

    @Mock JetonEmailRepository repository;

    JetonEmailService service;
    Utilisateur sara;
    /** Petite « base » en mémoire : empreinte -> jeton enregistré. */
    final Map<String, JetonEmail> base = new HashMap<>();

    @BeforeEach
    void preparer() {
        service = new JetonEmailService(repository);
        sara = Donnees.utilisateur(2, "Sara", Role.PASSAGER);
        when(repository.save(any(JetonEmail.class))).thenAnswer(appel -> {
            JetonEmail j = appel.getArgument(0);
            base.put(j.getEmpreinte(), j);
            return j;
        });
        when(repository.findByEmpreinte(anyString())).thenAnswer(appel -> Optional.ofNullable(base.get(appel.getArgument(0))));
    }

    @Test
    void unLienValideRenvoieSonProprietaireUneSeuleFois() {
        String jeton = service.creer(sara, TypeJeton.REINITIALISATION_MOT_DE_PASSE, Duration.ofHours(1));

        assertSame(sara, service.consommer(jeton, TypeJeton.REINITIALISATION_MOT_DE_PASSE));
        assertThrows(ApiException.class, () -> service.consommer(jeton, TypeJeton.REINITIALISATION_MOT_DE_PASSE),
                "un lien ne sert qu'une fois");
    }

    @Test
    void leJetonEnClairNEstPasEnregistre() {
        String jeton = service.creer(sara, TypeJeton.VERIFICATION_EMAIL, Duration.ofHours(1));

        assertFalse(base.containsKey(jeton));
        assertEquals(64, base.keySet().iterator().next().length(), "empreinte SHA-256 en hexadécimal");
    }

    @Test
    void unLienExpireEstRefuse() {
        String jeton = service.creer(sara, TypeJeton.VERIFICATION_EMAIL, Duration.ofHours(1));
        base.values().forEach(j -> j.setExpiration(LocalDateTime.now().minusMinutes(1)));

        assertThrows(ApiException.class, () -> service.consommer(jeton, TypeJeton.VERIFICATION_EMAIL));
    }

    @Test
    void unLienDeVerificationNeSertPasAChangerLeMotDePasse() {
        String jeton = service.creer(sara, TypeJeton.VERIFICATION_EMAIL, Duration.ofHours(1));

        assertThrows(ApiException.class, () -> service.consommer(jeton, TypeJeton.REINITIALISATION_MOT_DE_PASSE));
    }

    @Test
    void unLienInventeEstRefuse() {
        assertThrows(ApiException.class, () -> service.consommer("n-importe-quoi", TypeJeton.VERIFICATION_EMAIL));
        assertThrows(ApiException.class, () -> service.consommer(null, TypeJeton.VERIFICATION_EMAIL));
    }
}
