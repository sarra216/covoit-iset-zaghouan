package tn.iset.covoit.dto;

/**
 * Réponse à l'inscription / la connexion : le jeton JWT et le profil.
 * verificationRequise = true : le compte est créé mais l'email doit être confirmé (token vaut alors null).
 */
public record ConnexionResponse(String token, UtilisateurResponse utilisateur, boolean verificationRequise) {

    public static ConnexionResponse connecte(String token, UtilisateurResponse utilisateur) {
        return new ConnexionResponse(token, utilisateur, false);
    }

    public static ConnexionResponse aVerifier(UtilisateurResponse utilisateur) {
        return new ConnexionResponse(null, utilisateur, true);
    }
}
