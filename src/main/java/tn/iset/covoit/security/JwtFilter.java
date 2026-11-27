package tn.iset.covoit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.iset.covoit.repository.UtilisateurRepository;

import java.io.IOException;
import java.util.List;

/**
 * Lit l'en-tête « Authorization: Bearer <jeton> » de chaque requête.
 * Si le jeton est valide et que le compte est actif, l'utilisateur est connecté pour cette requête.
 * Un compte suspendu (ou supprimé) est refusé immédiatement, même avec un jeton encore valide.
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;

    public JwtFilter(JwtService jwtService, UtilisateurRepository utilisateurRepository) {
        this.jwtService = jwtService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse, FilterChain chaine)
            throws ServletException, IOException {
        String entete = requete.getHeader("Authorization");
        if (entete != null && entete.startsWith("Bearer ")) {
            Long utilisateurId = jwtService.verifier(entete.substring(7).trim());
            if (utilisateurId != null) {
                utilisateurRepository.findById(utilisateurId)
                        .filter(u -> !u.isSuspendu())
                        .ifPresent(u -> {
                            // Le « principal » est l'id ; le rôle ADMIN donne accès à /api/admin/**
                            var droits = u.estAdmin() ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN")) : List.<SimpleGrantedAuthority>of();
                            SecurityContextHolder.getContext().setAuthentication(
                                    new UsernamePasswordAuthenticationToken(u.getId(), null, droits));
                        });
            }
        }
        chaine.doFilter(requete, reponse);
    }
}
