package tn.iset.covoit.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import tn.iset.covoit.repository.UtilisateurRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Règles de sécurité :
 *  - /api/auth/** (inscription, connexion, mot de passe oublié) : ouvert à tous ;
 *  - /api/admin/** : jeton d'un compte ADMIN, sinon 403 ;
 *  - le reste de /api/** : jeton JWT obligatoire, sinon 401 ;
 *  - pages HTML, CSS, JS : publiques (chaque page redirige vers la connexion si besoin).
 * Pas de session côté serveur (STATELESS) : chaque requête porte son jeton.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtres(HttpSecurity http, JwtService jwtService, UtilisateurRepository utilisateurRepository)
            throws Exception {
        http
                // CSRF vise les cookies de session ; ici l'authentification passe par un en-tête, donc inutile
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regles -> regles
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint((requete, reponse, erreur) -> {
                    reponse.setStatus(401);
                    reponse.setContentType("application/json");
                    reponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    reponse.getWriter().write("{\"message\":\"Session expirée, veuillez vous reconnecter\"}");
                }).accessDeniedHandler((requete, reponse, erreur) -> {
                    reponse.setStatus(403);
                    reponse.setContentType("application/json");
                    reponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    reponse.getWriter().write("{\"message\":\"Accès réservé à l'administration\"}");
                }))
                .addFilterBefore(new JwtFilter(jwtService, utilisateurRepository), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
