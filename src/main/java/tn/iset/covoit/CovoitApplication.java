package tn.iset.covoit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Pas d'utilisateur « user » généré par Spring Security : on s'authentifie avec nos propres jetons JWT
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling // rappels de départ (#9) et expiration des demandes
public class CovoitApplication {

    public static void main(String[] args) {
        SpringApplication.run(CovoitApplication.class, args);
    }
}
